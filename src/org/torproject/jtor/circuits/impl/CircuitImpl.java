package org.torproject.jtor.circuits.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.torproject.jtor.TorException;
import org.torproject.jtor.circuits.Circuit;
import org.torproject.jtor.circuits.CircuitBuildHandler;
import org.torproject.jtor.circuits.CircuitNode;
import org.torproject.jtor.circuits.Connection;
import org.torproject.jtor.circuits.ConnectionClosedException;
import org.torproject.jtor.circuits.OpenStreamResponse;
import org.torproject.jtor.circuits.cells.Cell;
import org.torproject.jtor.circuits.cells.RelayCell;
import org.torproject.jtor.crypto.TorRandom;
import org.torproject.jtor.data.IPv4Address;
import org.torproject.jtor.directory.Router;
import org.torproject.jtor.logging.Logger;

/**
 * This class represents an established circuit through the Tor network.
 *
 */
public class CircuitImpl implements Circuit {
	static CircuitImpl create(CircuitManagerImpl circuitManager, ConnectionManagerImpl connectionManager, Logger logger) {
		return new CircuitImpl(circuitManager, connectionManager, logger);
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
	private final Logger logger;
	private final List<CircuitNodeImpl> nodeList;
	private final BlockingQueue<RelayCell> relayCellResponseQueue;
	private final BlockingQueue<Cell> controlCellResponseQueue;
	private final Map<Integer, StreamImpl> streamMap;
	private final CircuitBuilder circuitBuilder;
	private final Object relaySendLock = new Object();
	private int currentStreamId;
	private CircuitState state = CircuitState.UNCONNECTED;
	private Date circuitBuildStart;

	private CircuitImpl(CircuitManagerImpl circuitManager, ConnectionManagerImpl connectionManager, Logger logger) {
		nodeList = new ArrayList<CircuitNodeImpl>();
		this.circuitManager = circuitManager;
		this.logger = logger;
		this.relayCellResponseQueue = new LinkedBlockingQueue<RelayCell>();
		this.controlCellResponseQueue = new LinkedBlockingQueue<Cell>();
		this.streamMap = new HashMap<Integer, StreamImpl>();
		circuitBuilder = new CircuitBuilder(this, connectionManager, logger);
		initializeCurrentStreamId();
	}

	private void initializeCurrentStreamId() {
		final TorRandom random = new TorRandom();
		currentStreamId = random.nextInt(0xFFFF) + 1;	
	}
	
	void initializeConnectingCircuit(ConnectionImpl entryConnection, int circuitId) {
		this.circuitId = circuitId;
		this.entryConnection = entryConnection;
	}
	
	public boolean isConnected() {
		return state == CircuitState.OPEN;
	}

	void setConnected() {
		state = CircuitState.OPEN;
	}
	
	public void openCircuit(List<Router> circuitPath, CircuitBuildHandler handler)  {
		startCircuitOpen();	
		if(circuitBuilder.openCircuit(circuitPath, handler)) 
			circuitOpenSucceeded();
		else
			circuitOpenFailed();
	}

	private void startCircuitOpen() {
		if(state != CircuitState.UNCONNECTED)
			throw new IllegalStateException("Can only connect UNCONNECTED circuits");
		circuitBuildStart = new Date();
		state = CircuitState.BUILDING;
		circuitManager.circuitStartConnect(this);
	}
	
	private void circuitOpenFailed() {
		state = CircuitState.FAILED;
		circuitManager.circuitClosed(this);
	}
	
	private void circuitOpenSucceeded() {
		state = CircuitState.OPEN;
		circuitManager.circuitConnected(this);
	}

	public void extendCircuit(Router router) {
		if(!isConnected())
			throw new TorException("Cannot extend an unconnected circuit");
		circuitBuilder.extendTo(router);
	}

	public Connection getConnection() {
		if(!isConnected())
			throw new TorException("Circuit is not connected.");
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
		synchronized(relaySendLock) {
			logger.debug("Sending:     "+ cell);
			cell.setLength();
			targetNode.updateForwardDigest(cell);
			cell.setDigest(targetNode.getForwardDigestBytes());

			for(CircuitNode node = targetNode; node != null; node = node.getPreviousNode())
				node.encryptForwardCell(cell);

			sendCell(cell);
		}
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
			return null;
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

	public OpenStreamResponse openDirectoryStream() {
		return null;
	}

	public OpenStreamResponse openExitStream(IPv4Address address, int port) {
		return openExitStream(address.toString(), port);
	}

	public OpenStreamResponse openExitStream(String target, int port) {
		final StreamImpl stream = createNewStream();
		try {
			stream.openExit(target, port);
			return OpenStreamResponseImpl.createStreamOpened(stream);
		} catch(TorException e) {
			logger.info("Failed to open stream: "+ e.getMessage());
			removeStream(stream);
			return OpenStreamResponseImpl.createStreamError();
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
