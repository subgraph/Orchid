package org.torproject.jtor.circuits.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.torproject.jtor.Logger;
import org.torproject.jtor.TorException;
import org.torproject.jtor.circuits.Circuit;
import org.torproject.jtor.circuits.CircuitBuildHandler;
import org.torproject.jtor.circuits.CircuitNode;
import org.torproject.jtor.circuits.Connection;
import org.torproject.jtor.circuits.ConnectionClosedException;
import org.torproject.jtor.circuits.ConnectionConnectException;
import org.torproject.jtor.circuits.Stream;
import org.torproject.jtor.circuits.cells.Cell;
import org.torproject.jtor.circuits.cells.RelayCell;
import org.torproject.jtor.data.IPv4Address;
import org.torproject.jtor.directory.Router;

/**
 * This class represents an established circuit through the Tor network.
 *
 */
public class CircuitImpl implements Circuit {
	static CircuitImpl create(CircuitManagerImpl circuitManager, ConnectionManagerImpl connectionManager, Logger logger) {
		return new CircuitImpl(circuitManager, connectionManager, logger);
	}

	private static ConnectionImpl createEntryConnection(ConnectionManagerImpl connectionManager, Router router) {
		final ConnectionImpl existingConnection = connectionManager.findActiveLinkForRouter(router);
		if(existingConnection != null)
			return existingConnection;
		else
			return connectionManager.createConnection(router);
	}

	private enum CircuitState {
		UNCONNECTED("Unconnected"),
		BUILDING("Building"),
		FAILED("Failed"),
		OPEN("Open"),
		DESTROYED("Destroyed");
		String name;
		CircuitState(String name) { this.name = name; }
		public String toString() { return name; }
	}

	private final static long CIRCUIT_BUILD_TIMEOUT_MS = 30 * 1000;
	private final static long CIRCUIT_RELAY_RESPONSE_TIMEOUT = 20 * 1000;

	private ConnectionImpl entryConnection;
	private int circuitId;
	private final CircuitManagerImpl circuitManager;
	private final ConnectionManagerImpl connectionManager;
	private CircuitBuilder circuitBuilder;
	private final Logger logger;
	private final List<CircuitNodeImpl> nodeList;
	private final BlockingQueue<RelayCell> relayCellResponseQueue;
	private final BlockingQueue<Cell> controlCellResponseQueue;
	private final Map<Integer, StreamImpl> streamMap;
	private int currentStreamId;
	private CircuitState state = CircuitState.UNCONNECTED;
	private Date circuitBuildStart;

	// XXX implement control relay lock
	private CircuitImpl(CircuitManagerImpl circuitManager, ConnectionManagerImpl connectionManager, Logger logger) {
		nodeList = new ArrayList<CircuitNodeImpl>();
		this.circuitManager = circuitManager;
		this.connectionManager = connectionManager;
		this.logger = logger;
		this.relayCellResponseQueue = new LinkedBlockingQueue<RelayCell>();
		this.controlCellResponseQueue = new LinkedBlockingQueue<Cell>();
		this.streamMap = new HashMap<Integer, StreamImpl>();
	}

	public boolean isConnected() {
		return state == CircuitState.OPEN;
	}

	void setConnected() {
		state = CircuitState.OPEN;
	}

	public boolean openCircuit(List<Router> circuitPath, CircuitBuildHandler handler)  {
		if(circuitPath.isEmpty())
			throw new IllegalArgumentException("Path must contain at least one router to create a circuit.");
		entryConnection = createEntryConnection(connectionManager, circuitPath.get(0));
		circuitId = entryConnection.allocateCircuitId(this);
		circuitBuilder = new CircuitBuilder(this, circuitPath);

		state = CircuitState.BUILDING;
		circuitBuildStart = new Date();
		circuitManager.circuitStartConnect(this);
		if(!entryConnection.isConnected()) {
			try {
				final Date connectStart = new Date();
				entryConnection.connect();
				final Date now = new Date();
				logger.debug("Connect completed in " + (now.getTime() - connectStart.getTime()) +" milliseconds");
			} catch(ConnectionConnectException e) {
				entryConnection.removeCircuit(this);
				state = CircuitState.FAILED;
				circuitManager.circuitClosed(this);
				if(handler != null)
					handler.connectionFailed(e.getMessage());
				return false;
			}
		}

		if(handler != null)
			handler.connectionCompleted(entryConnection);

		if(circuitBuilder.build(handler)) {
			circuitManager.circuitConnected(this);
			return true;
		}
		entryConnection.removeCircuit(this);
		state = CircuitState.FAILED;
		circuitManager.circuitClosed(this);
		return false;
	}

	public void extendCircuit(Router router) {
		if(!isConnected())
			throw new TorException("Cannot extend an unconnected circuit");
		circuitBuilder.extendTo(router);
	}

	public Connection getConnection() {
		return entryConnection;
	}

	public int getCircuitId() {
		return circuitId;
	}

	public void sendRelayCell(RelayCell cell) {
		sendRelayCellTo(cell, cell.getCircuitNode());
	}

	public void sendRelayCellToFinalNode(RelayCell cell) {
		sendRelayCellTo(cell, getFinalCircuitNode());
	}

	private void sendRelayCellTo(RelayCell cell, CircuitNode targetNode) {
		logger.debug("Sending:     "+ cell);
		cell.setLength();
		targetNode.updateForwardDigest(cell);
		cell.setDigest(targetNode.getForwardDigestBytes());

		for(CircuitNode node = targetNode; node != null; node = node.getPreviousNode())
			node.encryptForwardCell(cell);

		sendCell(cell);
	}

	public void sendCell(Cell cell) {
		try {
			entryConnection.sendCell(cell);
		} catch (ConnectionClosedException e) {
			destroyCircuit();
		}
	}

	void appendNode(CircuitNodeImpl node) {
		nodeList.add(node);
	}

	int getCircuitLength() {
		return nodeList.size();
	}

	public CircuitNodeImpl getFinalCircuitNode() {
		if(nodeList.isEmpty())
			throw new TorException("getFinalCircuitNode() called on empty circuit");
		return nodeList.get( getCircuitLength() - 1);
	}

	public RelayCell createRelayCell(int relayCommand, int streamId, CircuitNode targetNode) {
		return new RelayCellImpl(targetNode, circuitId, streamId, relayCommand);
	}

	public RelayCell receiveRelayCell() {
		return dequeueRelayResponseCell();
	}

	private RelayCell dequeueRelayResponseCell() {
		try {
			final long timeout = getReceiveTimeout();
			return relayCellResponseQueue.poll(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new TorException("Interrupted while waiting for relay response");
		}
	}

	private RelayCell decryptRelayCell(Cell cell) {
		for(CircuitNodeImpl node: nodeList) {
			if(node.decryptBackwardCell(cell)) {
				return RelayCellImpl.createFromCell(node, cell);
			}
		}
		destroyCircuit();
		throw new TorException("Could not decrypt relay cell");
	}

	// Return null on timeout
	Cell receiveControlCellResponse() {
		try {
			final long timeout = getReceiveTimeout();
			return controlCellResponseQueue.poll(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return null;
		}
	}

	private long getReceiveTimeout() {
		if(state == CircuitState.BUILDING)
			return remainingBuildTime();
		else
			return CIRCUIT_RELAY_RESPONSE_TIMEOUT;
	}

	private long remainingBuildTime() {
		if(circuitBuildStart == null)
			return 0;
		final Date now = new Date();
		final long elapsed = now.getTime() - circuitBuildStart.getTime();
		if(elapsed >= CIRCUIT_BUILD_TIMEOUT_MS)
			return 0;
		return CIRCUIT_BUILD_TIMEOUT_MS - elapsed;
	}

	/*
	 * This is called by the cell reading thread in ConnectionImpl to deliver control cells 
	 * associated with this circuit (CREATED or CREATED_FAST).
	 */
	void deliverControlCell(Cell cell) {
		controlCellResponseQueue.add(cell);
	}

	/* This is called by the cell reading thread in ConnectionImpl to deliver RELAY cells. */
	void deliverRelayCell(Cell cell) {
		final RelayCell relayCell = decryptRelayCell(cell);
		logger.debug("Dispatching: "+ relayCell);
		switch(relayCell.getRelayCommand()) {
		case RelayCell.RELAY_EXTENDED:
		case RelayCell.RELAY_RESOLVED:
		case RelayCell.RELAY_CONNECTED:
		case RelayCell.RELAY_TRUNCATED:
			relayCellResponseQueue.add(relayCell);
			break;	
		case RelayCell.RELAY_DATA:
		case RelayCell.RELAY_END:
			processRelayDataCell(relayCell);
			break;

		case RelayCell.RELAY_SENDME:
			// XXX ignore for now
			break;
		case RelayCell.RELAY_BEGIN:
		case RelayCell.RELAY_BEGIN_DIR:
		case RelayCell.RELAY_EXTEND:
		case RelayCell.RELAY_RESOLVE:
		case RelayCell.RELAY_TRUNCATE:
			destroyCircuit();
			throw new TorException("Unexpected 'forward' direction relay cell type: "+ relayCell.getRelayCommand());
		}
	}

	/* Runs in the context of the connection cell reading thread */
	private void processRelayDataCell(RelayCell cell) {
		synchronized(streamMap) {
			final StreamImpl stream = streamMap.get(cell.getStreamId());
			if(stream == null)
				throw new TorException("Stream not found for data cell with stream id: "+ cell.getStreamId());
			stream.addInputCell(cell);
		}
	}

	public Stream openDirectoryStream() {
		final StreamImpl stream = createNewStream();
		stream.openDirectory();
		return stream;
	}

	public Stream openExitStream(IPv4Address address, int port) {
		return openExitStream(address.toString(), port);
	}

	public Stream openExitStream(String target, int port) {
		final StreamImpl stream = createNewStream();
		try {
			stream.openExit(target, port);
			return stream;

		} catch(TorException e) {
			System.out.println("Tor exception "+ e);
			e.printStackTrace();
			removeStream(stream);
			return null;
		}
	}

	boolean isFinalNodeDirectory() {
		return getFinalCircuitNode().getRouter().getDirectoryPort() != 0;
	}

	void destroyCircuit() {
		state = CircuitState.DESTROYED;
		entryConnection.removeCircuit(this);
		synchronized(streamMap) {
			for(StreamImpl s: streamMap.values())
				s.close();
		}
		circuitManager.circuitClosed(this);
	}

	private StreamImpl createNewStream() {
		synchronized(streamMap) {
			final int streamId = allocateStreamId();
			final StreamImpl stream = new StreamImpl(this, getFinalCircuitNode(), streamId);
			streamMap.put(streamId, stream);
			return stream;
		}
	}

	private int allocateStreamId() {
		currentStreamId++;
		if(currentStreamId > 0xFFFF)
			currentStreamId = 1;
		return currentStreamId;
	}

	void removeStream(StreamImpl stream) {
		synchronized(streamMap) {
			streamMap.remove(stream.getStreamId());
		}
	}

	public String toString() {
		return "Circuit id="+ circuitId +" state=" + state +" "+ pathToString();
	}

	private  String pathToString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("[");
		for(CircuitNode node: nodeList) {
			if(sb.length() > 1)
				sb.append(",");
			sb.append(node.toString());
		}
		sb.append("]");
		return sb.toString();
	}
}
