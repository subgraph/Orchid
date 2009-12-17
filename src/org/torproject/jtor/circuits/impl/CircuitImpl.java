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
import org.torproject.jtor.circuits.ConnectionConnectException;
import org.torproject.jtor.circuits.Stream;
import org.torproject.jtor.circuits.cells.Cell;
import org.torproject.jtor.circuits.cells.RelayCell;
import org.torproject.jtor.directory.Router;

/**
 * This class represents an established circuit through the Tor network.
 *
 */
public class CircuitImpl implements Circuit {
	static CircuitImpl create(CircuitManagerImpl circuitManager, ConnectionManagerImpl connectionManager, List<Router> routerPath) {
		if(routerPath.isEmpty())
			throw new IllegalArgumentException("Path must contain at least one router to create a circuit.");
		final ConnectionImpl entryConnection = createEntryConnection(connectionManager, routerPath.get(0));
		return new CircuitImpl(circuitManager, entryConnection, routerPath);
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
		OPEN("Open");
		String name;
		CircuitState(String name) { this.name = name; }
		public String toString() { return name; }

	}
	private final static long CIRCUIT_BUILD_TIMEOUT_MS = 60 * 1000;

	private final ConnectionImpl entryConnection;
	private final int circuitId;
	private final CircuitManagerImpl circuitManager;
	private final CircuitBuilder circuitBuilder;
	private final Map<Router, CircuitNodeImpl> circuitNodes;
	private final List<CircuitNodeImpl> nodeList;
	private final BlockingQueue<RelayCell> relayCellResponseQueue;
	private final BlockingQueue<Cell> controlCellResponseQueue;
	private final Map<Integer, StreamImpl> streamMap;
	private int currentStreamId;
	private boolean truncateRequested = false;
	private CircuitState state = CircuitState.UNCONNECTED;
	private Date circuitBuildStart;

	// XXX implement control relay lock
	private CircuitImpl(CircuitManagerImpl circuitManager, ConnectionImpl entryConnection, List<Router> circuitPath) {
		circuitNodes = new HashMap<Router, CircuitNodeImpl>();
		nodeList = new ArrayList<CircuitNodeImpl>();
		circuitId = entryConnection.allocateCircuitId(this);
		this.circuitManager = circuitManager;
		this.entryConnection = entryConnection;
		this.circuitBuilder = new CircuitBuilder(this, circuitPath);
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
	public boolean openCircuit(CircuitBuildHandler handler)  {
		state = CircuitState.BUILDING;
		circuitBuildStart = new Date();
		circuitManager.circuitStartConnect(this);
		if(!entryConnection.isConnected()) {
			try {
				entryConnection.connect();
			} catch(ConnectionConnectException e) {
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
			e.printStackTrace();
		}
	}

	void appendNode(CircuitNodeImpl node) {
		circuitNodes.put(node.getRouter(), node);
		nodeList.add(node);
	}

	int getCircuitLength() {
		return nodeList.size();
	}

	CircuitNodeImpl getFinalCircuitNode() {
		if(nodeList.isEmpty())
			throw new TorException("getFinalCircuitNode() called on empty circuit");
		return nodeList.get( getCircuitLength() - 1);
		
	}

	public RelayCell createRelayCell(int relayCommand, int streamId, CircuitNode targetNode) {
		return new RelayCellImpl(targetNode, circuitId, streamId, relayCommand);
	}

	public RelayCell receiveRelayResponse(int expectedType) {
		final RelayCell relayCell = dequeueRelayResponseCell();
		if(relayCell.getRelayCommand() == expectedType)
			return relayCell;
		throw new TorException("Received unexpected relay response type: "+ relayCell.getRelayCommand());
	}

	private RelayCell dequeueRelayResponseCell() {
		try {
			return relayCellResponseQueue.take();
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
		throw new TorException("Could not decrypt relay cell");
	}

	// Return null on timeout
	Cell receiveControlCellResponse() {
		try {
			final long timeout = getReceiveTimeout();
			return controlCellResponseQueue.poll(timeout, TimeUnit.MILLISECONDS);			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private long getReceiveTimeout() {
		if(state == CircuitState.BUILDING)
			return remainingBuildTime();
		
		throw new TorException("Implement me for state "+ state);	
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
	 * Should only be CREATED or CREATED_FAST
	 */
	void deliverControlCell(Cell cell) {
		controlCellResponseQueue.add(cell);
	}

	/* Runs in the context of the ConnectionImpl cell reading thread */
	void deliverRelayCell(Cell cell) {
		final RelayCell relayCell = decryptRelayCell(cell);

		switch(relayCell.getRelayCommand()) {

		case RelayCell.RELAY_EXTENDED:
		case RelayCell.RELAY_RESOLVED:
		case RelayCell.RELAY_CONNECTED:
			relayCellResponseQueue.add(relayCell);
			break;	
		case RelayCell.RELAY_DATA:
			processRelayDataCell(relayCell);
			break;
		case RelayCell.RELAY_END:
			processRelayEndCell(relayCell);
			break;
		case RelayCell.RELAY_TRUNCATED:
			if(truncateRequested)
				relayCellResponseQueue.add(relayCell);
			else
				processGratuitousRelayTruncatedCell(relayCell);
			break;
		case RelayCell.RELAY_BEGIN:
		case RelayCell.RELAY_BEGIN_DIR:
		case RelayCell.RELAY_EXTEND:
		case RelayCell.RELAY_RESOLVE:
		case RelayCell.RELAY_TRUNCATE:
		case RelayCell.RELAY_SENDME:
			throw new TorException("Unexpected 'forward' direction relay cell type: "+ relayCell.getRelayCommand());
		}
		
	}

	/* Runs in the context of the connection cell reading thread */
	private void processRelayDataCell(RelayCell cell) {
		final StreamImpl stream = streamMap.get(cell.getStreamId());
		if(stream == null) 
			throw new TorException("Stream not found for data cell with stream id: "+ cell.getStreamId());
		stream.addInputCell(cell);
	}

	/* Runs in the context of the connection cell reading thread */
	private void processRelayEndCell(RelayCell cell) {
		System.out.println("RELAY END [implement me]");
	}

	/* Runs in the context of the connection cell reading thread */
	private void processGratuitousRelayTruncatedCell(RelayCell cell) {
		System.out.println("(Gratuitous) RELAY TRUNCATED [implement me] with code="+ (cell.getByte() & 0xFF) +" on circuit: "+ this);
	}

	public Stream openDirectoryStream() {
		final StreamImpl stream = createNewStream();
		stream.openDirectory();
		return stream;
	}

	boolean isFinalNodeDirectory() {
		return getFinalCircuitNode().getRouter().getDirectoryPort() != 0;
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
	public boolean equals(Object o) {
		if(!(o instanceof CircuitImpl))
			return false;
		final CircuitImpl other = (CircuitImpl) o;
		return (other.circuitId == this.circuitId && other.entryConnection == this.entryConnection);
	}

	public int hashCode() {
		return this.circuitId;
	}

}
