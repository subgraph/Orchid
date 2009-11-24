package org.torproject.jtor.circuits.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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
	static CircuitImpl create(ConnectionManagerImpl connectionManager, List<Router> routerPath) {
		if(routerPath.isEmpty())
			throw new IllegalArgumentException("Path must contain at least one router to create a circuit.");
		final ConnectionImpl entryConnection = createEntryConnection(connectionManager, routerPath.get(0));
		return new CircuitImpl(entryConnection, routerPath);
	}
	
	private static ConnectionImpl createEntryConnection(ConnectionManagerImpl connectionManager, Router router) {
		final ConnectionImpl existingConnection = connectionManager.findActiveLinkForRouter(router);
		if(existingConnection != null)
			return existingConnection;
		else
			return connectionManager.createConnection(router);
	}
	
	private final ConnectionImpl entryConnection;
	private final int circuitId;
	private boolean isConnected;
	private final CircuitBuilder circuitBuilder;
	private final Map<Router, CircuitNodeImpl> circuitNodes;
	private final List<CircuitNodeImpl> nodeList;
	private final BlockingQueue<RelayCell> relayCellResponseQueue;
	private final BlockingQueue<Cell> controlCellResponseQueue;
	private final Map<Integer, StreamImpl> streamMap;
	private int currentStreamId;
	private boolean truncateRequested = false;
	// XXX implement control relay lock
	private CircuitImpl(ConnectionImpl entryConnection, List<Router> circuitPath) {
		circuitNodes = new HashMap<Router, CircuitNodeImpl>();
		nodeList = new ArrayList<CircuitNodeImpl>();
		circuitId = entryConnection.allocateCircuitId(this);
		this.entryConnection = entryConnection;
		this.circuitBuilder = new CircuitBuilder(this, circuitPath);
		this.relayCellResponseQueue = new LinkedBlockingQueue<RelayCell>();
		this.controlCellResponseQueue = new LinkedBlockingQueue<Cell>();
		this.streamMap = new HashMap<Integer, StreamImpl>();
	}
	
	public boolean isConnected() {
		return isConnected;
	}
	
	public boolean openCircuit(CircuitBuildHandler handler)  {
		if(!entryConnection.isConnected()) {
			try {
				entryConnection.connect();
			} catch(ConnectionConnectException e) {
				handler.connectionFailed(e.getMessage());
				return false;
			}
		}
		
		if(handler != null)
			handler.connectionCompleted(entryConnection);
		
		if(circuitBuilder.build(handler)) {
			isConnected = true;
			return true;
		}
		return false;
			
	}
	
	public void extendCircuit(Router router) {
		if(!isConnected)
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
		System.out.println("Sending relay cell to --> "+ targetNode.getRouter().getNickname());
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
	
	Cell receiveControlCellResponse() {
		try {
			return controlCellResponseQueue.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	/*
	 * Should only be CREATED or CREATED_FAST
	 */
	void deliverControlCell(Cell cell) {
		controlCellResponseQueue.add(cell);
	}
	
	/* Runs in the context of the connection cell reading thread */
	void deliverRelayCell(Cell cell) {
		final RelayCell relayCell = decryptRelayCell(cell);
		
		System.out.println("Delivering relay cell: "+ relayCell.getRelayCommand());
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
		System.out.println("(Gratuitous) RELAY TRUNCATED [implement me]");
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

}
