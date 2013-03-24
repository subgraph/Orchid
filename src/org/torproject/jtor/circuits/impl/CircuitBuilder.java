package org.torproject.jtor.circuits.impl;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.torproject.jtor.Tor;
import org.torproject.jtor.TorException;
import org.torproject.jtor.circuits.CircuitBuildHandler;
import org.torproject.jtor.circuits.CircuitNode;
import org.torproject.jtor.circuits.Connection;
import org.torproject.jtor.circuits.ConnectionFailedException;
import org.torproject.jtor.circuits.ConnectionHandshakeException;
import org.torproject.jtor.circuits.ConnectionIOException;
import org.torproject.jtor.circuits.ConnectionTimeoutException;
import org.torproject.jtor.circuits.cells.Cell;
import org.torproject.jtor.circuits.cells.RelayCell;
import org.torproject.jtor.connections.ConnectionCache;
import org.torproject.jtor.crypto.TorKeyAgreement;
import org.torproject.jtor.crypto.TorMessageDigest;
import org.torproject.jtor.data.HexDigest;
import org.torproject.jtor.directory.Router;

/*
 * Utility class used by CircuitImpl that manages setting up a circuit 
 * through a specified path of router nodes. 
 */
class CircuitBuilder {
	private final static Logger logger = Logger.getLogger(CircuitBuilder.class.getName());
	private final CircuitImpl circuit;
	private final ConnectionCache connectionCache;
	private final boolean isDirectoryCircuit;
	private final TorInitializationTracker initializationTracker;

	
	CircuitBuilder(CircuitImpl circuit, ConnectionCache connectionCache, boolean isDirectoryCircuit, TorInitializationTracker initializationTracker) {
		this.circuit = circuit;
		this.connectionCache = connectionCache;
		this.isDirectoryCircuit = isDirectoryCircuit;
		this.initializationTracker = initializationTracker;
	}
	
	boolean openCircuit(List<Router> circuitPath, CircuitBuildHandler handler, boolean isDirectoryCircuit) {
		if(circuitPath.isEmpty())
			throw new IllegalArgumentException("Path must contain at least one router to create a circuit.");
		final Router entryRouter = circuitPath.get(0);
		try {
			return openEntryNodeConnection(entryRouter, handler, isDirectoryCircuit) && 
				buildCircuit(circuitPath, handler);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			if(handler != null) {
				handler.connectionFailed("Connection interrupted");
			}
			return false;
		} catch (ConnectionIOException e) {
			if(handler != null) {
				handler.connectionFailed(e.getMessage());
			}
			return false;
		}
	}

	private boolean openEntryNodeConnection(Router entryRouter, CircuitBuildHandler handler, boolean isDirectoryCircuit) throws InterruptedException, ConnectionTimeoutException, ConnectionFailedException, ConnectionHandshakeException {
		final Connection entryConnection = connectionCache.getConnectionTo(entryRouter, isDirectoryCircuit);
			
		circuit.bindToConnection(entryConnection);
		
		if(handler != null) {
			handler.connectionCompleted(entryConnection);
		}

		return true;
	}

	private boolean buildCircuit(List<Router> circuitPath, CircuitBuildHandler handler) {
		try {
			runCircuitBuild(circuitPath, handler);
		} catch(TorException e) {
			e.printStackTrace();
			if(handler != null) 
				handler.circuitBuildFailed(e.getMessage());
			return false;
		} catch(Exception e) {
			logger.log(Level.WARNING, "Unexpected exception building circuit.", e);
			if(handler != null)
				handler.circuitBuildFailed("Unexpected exception: "+ e.getMessage());
			return false;
		}
		circuit.setConnected();
		if(handler != null)
			handler.circuitBuildCompleted(circuit);
		
		if(initializationTracker != null && !isDirectoryCircuit) {
			initializationTracker.notifyEvent(Tor.BOOTSTRAP_STATUS_DONE);
		}
	
		return true;
	}

	private void runCircuitBuild(List<Router> circuitPath, CircuitBuildHandler handler) {
		final Router entryRouter = circuitPath.get(0);
		final CircuitNode firstNode = createFastTo(entryRouter);
		if(handler != null)
			handler.nodeAdded(firstNode);

		for(int i = 1; i < circuitPath.size(); i++) {
			final CircuitNode extendedNode = extendTo(circuitPath.get(i));
			if(handler != null)
				handler.nodeAdded(extendedNode);
		}
	}

	private CircuitNode createTo(Router targetRouter) {
		notifyInitialization();
		final CircuitNodeImpl newNode = new CircuitNodeImpl(targetRouter, null);
		sendCreateCell(newNode);
		receiveAndProcessCreateResponse(newNode);
		return newNode;
	}
	
	private CircuitNode createFastTo(Router targetRouter) {
		notifyInitialization();
		final CircuitNodeImpl newNode = new CircuitNodeImpl(targetRouter, null);
		sendCreateFastCell(newNode);
		receiveAndProcessCreateFastResponse(newNode);
		return newNode;
	}

	private void notifyInitialization() {
		if(initializationTracker == null) {
			return;
		}
		initializationTracker.notifyEvent(isDirectoryCircuit ? Tor.BOOTSTRAP_STATUS_ONEHOP_CREATE : Tor.BOOTSTRAP_STATUS_CIRCUIT_CREATE);
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

	private void sendCreateCell(CircuitNodeImpl node) {
		final Cell cell = CellImpl.createCell(circuit.getCircuitId(), Cell.CREATE);
		cell.putByteArray(node.createOnionSkin());
		circuit.sendCell(cell);
	}

	
	private void receiveAndProcessCreateResponse(CircuitNodeImpl node) {
		final Cell cell = circuit.receiveControlCellResponse();
		if(cell == null)
			throw new TorException("Timeout building circuit");

		processCreatedCell(node, cell);
		circuit.appendNode(node);
	}

	private void processCreatedCell(CircuitNodeImpl node, Cell cell) {
		final byte[] cellBytes = cell.getCellBytes();
		final byte[] dhData = new byte[TorKeyAgreement.DH_LEN];
		final byte[] hash = new byte[TorMessageDigest.TOR_DIGEST_SIZE];

		int offset = Cell.CELL_HEADER_LEN;
		System.arraycopy(cellBytes, offset, dhData, 0, TorKeyAgreement.DH_LEN);

		offset += TorKeyAgreement.DH_LEN;
		System.arraycopy(cellBytes, offset, hash, 0, TorMessageDigest.TOR_DIGEST_SIZE);

		BigInteger peerPublic = new BigInteger(1, dhData);
		node.setSharedSecret(peerPublic, HexDigest.createFromDigestBytes(hash));
	}

	CircuitNode extendTo(Router targetRouter) {
		if(circuit.getCircuitLength() == 0)
			throw new TorException("Cannot EXTEND an empty circuit");
		final CircuitNodeImpl newNode = createExtendNode(targetRouter);
		final RelayCell cell = createRelayExtendCell(newNode);
		circuit.sendRelayCellToFinalNode(cell);
		try {
			receiveExtendResponse(newNode);
			circuit.appendNode(newNode);
			return newNode;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
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

	private void receiveExtendResponse(CircuitNodeImpl newNode) throws IOException {
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
