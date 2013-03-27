package org.torproject.jtor.circuits.impl;

import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.torproject.jtor.Tor;
import org.torproject.jtor.TorException;
import org.torproject.jtor.circuits.CircuitNode;
import org.torproject.jtor.circuits.Connection;
import org.torproject.jtor.circuits.ConnectionFailedException;
import org.torproject.jtor.circuits.ConnectionHandshakeException;
import org.torproject.jtor.circuits.ConnectionTimeoutException;
import org.torproject.jtor.circuits.cells.Cell;
import org.torproject.jtor.circuits.cells.RelayCell;
import org.torproject.jtor.connections.ConnectionCache;
import org.torproject.jtor.crypto.TorKeyAgreement;
import org.torproject.jtor.crypto.TorMessageDigest;
import org.torproject.jtor.data.HexDigest;
import org.torproject.jtor.directory.Router;

public class CircuitBuildTask implements Runnable {
	private final static Logger logger = Logger.getLogger(CircuitBuildTask.class.getName());
	private final CircuitCreationRequest request;
	private final ConnectionCache connectionCache;
	private final TorInitializationTracker initializationTracker;
	private final CircuitImpl circuit;

	CircuitBuildTask(CircuitCreationRequest request, ConnectionCache connectionCache, TorInitializationTracker initializationTracker) {
		this.request = request;
		this.connectionCache = connectionCache;
		this.initializationTracker = initializationTracker;
		this.circuit = request.getCircuit();
	}

	public void run() {
		if(logger.isLoggable(Level.FINE)) {
			logger.fine("Opening a new circuit to "+ pathToString());
		}
		final Router firstRouter = request.getPathElement(0);
		try {
			circuit.notifyCircuitBuildStart(request);
			openEntryNodeConnection(firstRouter);
			buildCircuit(firstRouter);
			circuit.notifyCircuitBuildCompleted();
		} catch (ConnectionTimeoutException e) {
			connectionFailed("Timeout connecting to "+ firstRouter);
		} catch (ConnectionFailedException e) {
			connectionFailed("Connection failed to "+ firstRouter + " : " + e.getMessage());
		} catch (ConnectionHandshakeException e) {
			connectionFailed("Handshake error connectint to "+ firstRouter + " : " + e.getMessage());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			circuitBuildFailed("Circuit building thread interrupted");
		} catch (TorException e) {
			circuitBuildFailed(e.getMessage());
		} catch(Exception e) {
			circuitBuildFailed("Unexpected exception: "+ e);
			logger.log(Level.WARNING, "Unexpected exception while building circuit: "+ e, e);
		}
	}

	private String pathToString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("[");
		for(Router r: request.getPath()) {
			if(sb.length() > 1)
				sb.append(",");
			sb.append(r.getNickname());
		}
		sb.append("]");
		return sb.toString();
	}

	private void connectionFailed(String message) {
		request.connectionFailed(message);
		circuit.notifyCircuitBuildFailed();
	}
	
	private void circuitBuildFailed(String message) {
		request.circuitBuildFailed(message);
		circuit.notifyCircuitBuildFailed();
	}
	
	private void openEntryNodeConnection(Router firstRouter) throws ConnectionTimeoutException, ConnectionFailedException, ConnectionHandshakeException, InterruptedException {
		final Connection connection = connectionCache.getConnectionTo(firstRouter, request.isDirectoryCircuit());
		circuit.bindToConnection(connection);
		request.connectionCompleted(connection);
	}

	private void buildCircuit(Router firstRouter) throws TorException {
		final CircuitNode firstNode = createFastTo(firstRouter);
		request.nodeAdded(firstNode);
		
		for(int i = 1; i < request.getPathLength(); i++) {
			final CircuitNode extendedNode = extendTo(request.getPathElement(i));
			request.nodeAdded(extendedNode);
		}
		request.circuitBuildCompleted(circuit);
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
			final int event = request.isDirectoryCircuit() ? 
					Tor.BOOTSTRAP_STATUS_ONEHOP_CREATE : Tor.BOOTSTRAP_STATUS_CIRCUIT_CREATE;
			initializationTracker.notifyEvent(event);
		}
	}

	private void notifyDone() {
		if(initializationTracker != null && !request.isDirectoryCircuit()) {
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
			throw new TorException("Circuit build failed: "+ msg);
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
}
