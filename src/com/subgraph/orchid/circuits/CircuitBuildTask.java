package com.subgraph.orchid.circuits;

import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.subgraph.orchid.Cell;
import com.subgraph.orchid.CircuitNode;
import com.subgraph.orchid.Connection;
import com.subgraph.orchid.ConnectionCache;
import com.subgraph.orchid.ConnectionFailedException;
import com.subgraph.orchid.ConnectionHandshakeException;
import com.subgraph.orchid.ConnectionTimeoutException;
import com.subgraph.orchid.RelayCell;
import com.subgraph.orchid.Router;
import com.subgraph.orchid.Tor;
import com.subgraph.orchid.TorException;
import com.subgraph.orchid.circuits.cells.CellImpl;
import com.subgraph.orchid.circuits.cells.RelayCellImpl;
import com.subgraph.orchid.circuits.path.PathSelectionFailedException;
import com.subgraph.orchid.crypto.TorKeyAgreement;
import com.subgraph.orchid.crypto.TorMessageDigest;
import com.subgraph.orchid.data.HexDigest;

public class CircuitBuildTask implements Runnable {
	private final static Logger logger = Logger.getLogger(CircuitBuildTask.class.getName());
	private final CircuitCreationRequest creationRequest;
	private final ConnectionCache connectionCache;
	private final TorInitializationTracker initializationTracker;
	private final CircuitBase circuit;

	private Connection connection = null;
	
	public CircuitBuildTask(CircuitCreationRequest request, ConnectionCache connectionCache) {
		this(request, connectionCache, null);
	}

	public CircuitBuildTask(CircuitCreationRequest request, ConnectionCache connectionCache, TorInitializationTracker initializationTracker) {
		this.creationRequest = request;
		this.connectionCache = connectionCache;
		this.initializationTracker = initializationTracker;
		this.circuit = request.getCircuit();
	}

	public void run() {
		Router firstRouter = null;
		try {
			circuit.notifyCircuitBuildStart();
			creationRequest.choosePath();
			circuit.notifyCircuitPathChosen(creationRequest.getPath());
			if(logger.isLoggable(Level.FINE)) {
				logger.fine("Opening a new circuit to "+ pathToString(creationRequest));
			}
			firstRouter = creationRequest.getPathElement(0);
			openEntryNodeConnection(firstRouter);
			buildCircuit(firstRouter);
			circuit.notifyCircuitBuildCompleted();
		} catch (ConnectionTimeoutException e) {
			connectionFailed("Timeout connecting to "+ firstRouter);
		} catch (ConnectionFailedException e) {
			connectionFailed("Connection failed to "+ firstRouter + " : " + e.getMessage());
		} catch (ConnectionHandshakeException e) {
			connectionFailed("Handshake error connecting to "+ firstRouter + " : " + e.getMessage());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			circuitBuildFailed("Circuit building thread interrupted");
		} catch(PathSelectionFailedException e) { 
			circuitBuildFailed(e.getMessage());
		} catch (TorException e) {
			circuitBuildFailed(e.getMessage());
		} catch(Exception e) {
			circuitBuildFailed("Unexpected exception: "+ e);
			logger.log(Level.WARNING, "Unexpected exception while building circuit: "+ e, e);
		}
	}

	private String pathToString(CircuitCreationRequest ccr) {
		final StringBuilder sb = new StringBuilder();
		sb.append("[");
		for(Router r: ccr.getPath()) {
			if(sb.length() > 1)
				sb.append(",");
			sb.append(r.getNickname());
		}
		sb.append("]");
		return sb.toString();
	}

	private void connectionFailed(String message) {
		creationRequest.connectionFailed(message);
		circuit.notifyCircuitBuildFailed();
	}
	
	private void circuitBuildFailed(String message) {
		creationRequest.circuitBuildFailed(message);
		circuit.notifyCircuitBuildFailed();
		if(connection != null) {
			connection.removeCircuit(circuit);
		}
	}
	
	private void openEntryNodeConnection(Router firstRouter) throws ConnectionTimeoutException, ConnectionFailedException, ConnectionHandshakeException, InterruptedException {
		connection = connectionCache.getConnectionTo(firstRouter, creationRequest.isDirectoryCircuit());
		circuit.bindToConnection(connection);
		creationRequest.connectionCompleted(connection);
	}

	private void buildCircuit(Router firstRouter) throws TorException {
		final CircuitNode firstNode = createFastTo(firstRouter);
		creationRequest.nodeAdded(firstNode);
		
		for(int i = 1; i < creationRequest.getPathLength(); i++) {
			final CircuitNode extendedNode = extendTo(creationRequest.getPathElement(i));
			creationRequest.nodeAdded(extendedNode);
		}
		creationRequest.circuitBuildCompleted(circuit);
		notifyDone();
	}

	private CircuitNode createFastTo(Router targetRouter) {
		notifyInitialization();
		final CircuitNodeImpl newNode = new CircuitNodeImpl(targetRouter, null);
		sendCreateFastCell(newNode);
		receiveAndProcessCreateFastResponse(newNode);
		return newNode;
	}

	private void notifyInitialization() {
		if(initializationTracker != null) {
			final int event = creationRequest.isDirectoryCircuit() ? 
					Tor.BOOTSTRAP_STATUS_ONEHOP_CREATE : Tor.BOOTSTRAP_STATUS_CIRCUIT_CREATE;
			initializationTracker.notifyEvent(event);
		}
	}

	private void notifyDone() {
		if(initializationTracker != null && !creationRequest.isDirectoryCircuit()) {
			initializationTracker.notifyEvent(Tor.BOOTSTRAP_STATUS_DONE);
		}
	}

	private void sendCreateFastCell(CircuitNodeImpl node) {
		final Cell cell = CellImpl.createCell(circuit.getCircuitId(), Cell.CREATE_FAST);
		cell.putByteArray(node.getCreateFastPublicValue());
		circuit.sendCell(cell);
	}
	
	private void receiveAndProcessCreateFastResponse(CircuitNodeImpl node) {
		final Cell cell = circuit.receiveControlCellResponse();
		if(cell == null) {
			throw new TorException("Timeout building circuit");
		}

		processCreatedFastCell(node, cell);
		circuit.appendNode(node);
	}
	
	private void processCreatedFastCell(CircuitNodeImpl node, Cell cell) {
		final byte[] cellBytes = cell.getCellBytes();
		final byte[] yValue = new byte[TorMessageDigest.TOR_DIGEST_SIZE];
		final byte[] hash = new byte[TorMessageDigest.TOR_DIGEST_SIZE];
		int offset = Cell.CELL_HEADER_LEN;
		System.arraycopy(cellBytes, offset, yValue, 0, TorMessageDigest.TOR_DIGEST_SIZE);
		offset += TorMessageDigest.TOR_DIGEST_SIZE;
		System.arraycopy(cellBytes, offset, hash, 0, TorMessageDigest.TOR_DIGEST_SIZE);
		node.setCreatedFastValue(yValue, HexDigest.createFromDigestBytes(hash));
	}
	
	CircuitNode extendTo(Router targetRouter) {
		if(circuit.getCircuitLength() == 0)
			throw new TorException("Cannot EXTEND an empty circuit");
		final CircuitNodeImpl newNode = createExtendNode(targetRouter);
		final RelayCell cell = createRelayExtendCell(newNode);
		circuit.sendRelayCellToFinalNode(cell);

		receiveExtendResponse(newNode);
		circuit.appendNode(newNode);
		return newNode;
	}

	private CircuitNodeImpl createExtendNode(Router router) {
		return new CircuitNodeImpl(router, circuit.getFinalCircuitNode());
	}

	private RelayCell createRelayExtendCell(CircuitNodeImpl newNode) {
		final RelayCell cell = new RelayCellImpl(circuit.getFinalCircuitNode(), circuit.getCircuitId(), 0, RelayCell.RELAY_EXTEND, true);
		final Router router = newNode.getRouter();
		cell.putByteArray(router.getAddress().getAddressDataBytes());
		cell.putShort(router.getOnionPort());
		cell.putByteArray( newNode.createOnionSkin());
		cell.putByteArray(router.getIdentityKey().getFingerprint().getRawBytes());
		return cell;
	}

	private void receiveExtendResponse(CircuitNodeImpl newNode) {
		final RelayCell cell = circuit.receiveRelayCell();
		if(cell == null)
			throw new TorException("Timeout building circuit");

		final int command = cell.getRelayCommand();
		if(command == RelayCell.RELAY_TRUNCATED) {
			final int code = cell.getByte() & 0xFF;
			final String msg = CellImpl.errorToDescription(code);
			final String source = nodeToName(cell.getCircuitNode());
			final String extendTarget = nodeToName(newNode);
			if(code == Cell.ERROR_PROTOCOL) {
				logProtocolViolation(source, extendTarget, newNode.getRouter());
			}
			throw new TorException("Error from ("+ source + ") while extending to ("+ extendTarget +"): "+ msg);
		} else if (command != RelayCell.RELAY_EXTENDED) {
			throw new TorException("Unexpected response to RELAY_EXTEND.  Command = "+ command);
		}

		byte[] dhPublic = new byte[TorKeyAgreement.DH_LEN];
		cell.getByteArray(dhPublic);
		byte[] keyHash = new byte[TorMessageDigest.TOR_DIGEST_SIZE];
		cell.getByteArray(keyHash);
		HexDigest packetDigest = HexDigest.createFromDigestBytes(keyHash);
		BigInteger peerPublic = new BigInteger(1, dhPublic);
		newNode.setSharedSecret(peerPublic, packetDigest);
	}
	
	private void logProtocolViolation(String sourceName, String targetName, Router targetRouter) {
		final String version = (targetRouter == null) ? "(none)" : targetRouter.getVersion();
		logger.warning("Protocol error extending circuit from ("+ sourceName +") to ("+ targetName +") [version: "+ version +"]");
	}

	private String nodeToName(CircuitNode node) {
		if(node == null || node.getRouter() == null) {
			return "(null)";
		}
		final Router router = node.getRouter();
		return router.getNickname();
	}
}
