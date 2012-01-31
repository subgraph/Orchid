package org.torproject.jtor.circuits.impl;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import org.torproject.jtor.TorException;
import org.torproject.jtor.circuits.CircuitBuildHandler;
import org.torproject.jtor.circuits.CircuitNode;
import org.torproject.jtor.circuits.ConnectionConnectException;
import org.torproject.jtor.circuits.cells.Cell;
import org.torproject.jtor.circuits.cells.RelayCell;
import org.torproject.jtor.crypto.TorKeyAgreement;
import org.torproject.jtor.crypto.TorMessageDigest;
import org.torproject.jtor.data.HexDigest;
import org.torproject.jtor.directory.Router;
import org.torproject.jtor.logging.Logger;

/*
 * Utility class used by CircuitImpl that manages setting up a circuit 
 * through a specified path of router nodes. 
 */
class CircuitBuilder {

	private final CircuitImpl circuit;
	private final ConnectionManagerImpl connectionManager;
	private final Logger logger;

	
	CircuitBuilder(CircuitImpl circuit, ConnectionManagerImpl connectionManager, Logger logger) {
		this.circuit = circuit;
		this.connectionManager = connectionManager;
		this.logger = logger;
	}
	
	boolean openCircuit(List<Router> circuitPath, CircuitBuildHandler handler) {
		if(circuitPath.isEmpty())
			throw new IllegalArgumentException("Path must contain at least one router to create a circuit.");
		final Router entryRouter = circuitPath.get(0);
		return openEntryNodeConnection(entryRouter, handler) && 
			buildCircuit(circuitPath, handler);
	}
	
	private boolean openEntryNodeConnection(Router entryRouter, CircuitBuildHandler handler) {
		final ConnectionImpl entryConnection = createEntryConnection(entryRouter);
		if(!entryConnection.isConnected() && !connectEntryNodeConnection(entryConnection, handler))
			return false;
			
		final int circuitId = entryConnection.allocateCircuitId(circuit);
		circuit.initializeConnectingCircuit(entryConnection, circuitId);
		
		if(handler != null)
			handler.connectionCompleted(entryConnection);

		return true;
	}

	private boolean connectEntryNodeConnection(ConnectionImpl entryConnection, CircuitBuildHandler handler) {
		try {
			final Date start = new Date();
			entryConnection.connect();
			final Date now = new Date();
			logger.debug("Connect completed in "+ (now.getTime() - start.getTime()) +" milliseconds.");
			return true;
		} catch (ConnectionConnectException e) {
			processConnectFailed(entryConnection, handler, e);
			return false;
		} catch (Exception e) {
			processConnectFailed(entryConnection, handler, e);
			logger.error("Unexpected exception connecting to entry node.", e);
			return false;
		}
	}

	private void processConnectFailed(ConnectionImpl entryConnection, CircuitBuildHandler handler, Throwable e) {
		entryConnection.removeCircuit(circuit);
		if(handler != null)
			handler.connectionFailed(e.getMessage());
	}

	private ConnectionImpl createEntryConnection(Router entryRouter) {
		final ConnectionImpl existingConnection = connectionManager.findActiveLinkForRouter(entryRouter);
		if(existingConnection != null)
			return existingConnection;
		else
			return connectionManager.createConnection(entryRouter);
	}

	private boolean buildCircuit(List<Router> circuitPath, CircuitBuildHandler handler) {
		try {
			runCircuitBuild(circuitPath, handler);
		} catch(TorException e) {
			if(handler != null) 
				handler.circuitBuildFailed(e.getMessage());
			return false;
		} catch(Exception e) {
			logger.error("Unexpected exception building circuit.", e);
			if(handler != null)
				handler.circuitBuildFailed("Unexpected exception: "+ e.getMessage());
			return false;
		}
		circuit.setConnected();
		if(handler != null)
			handler.circuitBuildCompleted(circuit);
		return true;
	}

	private void runCircuitBuild(List<Router> circuitPath, CircuitBuildHandler handler) {
		final Router entryRouter = circuitPath.get(0);
		final CircuitNode firstNode = createTo(entryRouter);
		if(handler != null)
			handler.nodeAdded(firstNode);

		for(int i = 1; i < circuitPath.size(); i++) {
			final CircuitNode extendedNode = extendTo(circuitPath.get(i));
			if(handler != null)
				handler.nodeAdded(extendedNode);
		}
	}

	CircuitNode createTo(Router targetRouter) {
		final CircuitNodeImpl newNode = new CircuitNodeImpl(targetRouter, null);
		sendCreateCell(newNode);
		receiveAndProcessCreateResponse(newNode);
		return newNode;
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
		final RelayCell cell = new RelayCellImpl(circuit.getFinalCircuitNode(), circuit.getCircuitId(), 0, RelayCell.RELAY_EXTEND);
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
