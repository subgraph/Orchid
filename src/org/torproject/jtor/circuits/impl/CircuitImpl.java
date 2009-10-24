package org.torproject.jtor.circuits.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.torproject.jtor.TorException;
import org.torproject.jtor.circuits.Circuit;
import org.torproject.jtor.circuits.Connection;
import org.torproject.jtor.circuits.ConnectionClosedException;
import org.torproject.jtor.circuits.cells.Cell;
import org.torproject.jtor.circuits.cells.RelayCell;
import org.torproject.jtor.directory.RouterDescriptor;

/**
 * This class represents an established circuit through the Tor network.
 *
 */
public class CircuitImpl implements Circuit {
	public static CircuitImpl create(ConnectionManagerImpl connectionManager, List<RouterDescriptor> routerPath) {
		if(routerPath.isEmpty())
			throw new IllegalArgumentException("Path must contain at least one router to create a circuit.");
		final ConnectionImpl entryConnection = createEntryConnection(connectionManager, routerPath.get(0));
		return new CircuitImpl(entryConnection, routerPath);
		
	}
	
	private static ConnectionImpl createEntryConnection(ConnectionManagerImpl connectionManager, RouterDescriptor router) {
		final ConnectionImpl existingConnection = connectionManager.findActiveLinkForRouter(router);
		if(existingConnection != null)
			return existingConnection;
		else
			return connectionManager.createConnection(router);
	}
	
	private final ConnectionImpl entryConnection;
	private final int circuitId;
	private final CircuitBuilder circuitBuilder;
	private final Map<RouterDescriptor, CircuitNode> circuitNodes;
	private final List<CircuitNode> nodeList;
	private final BlockingQueue<Cell> relayCellQueue;
	private final BlockingQueue<Cell> controlCellQueue;
	
	private CircuitImpl(ConnectionImpl entryConnection, List<RouterDescriptor> circuitPath) {
		circuitNodes = new HashMap<RouterDescriptor, CircuitNode>();
		nodeList = new ArrayList<CircuitNode>();
		circuitId = entryConnection.allocateCircuitId(this);
		this.entryConnection = entryConnection;
		this.circuitBuilder = new CircuitBuilder(this, circuitPath);
		this.relayCellQueue = new LinkedBlockingQueue<Cell>();
		this.controlCellQueue = new LinkedBlockingQueue<Cell>();
	}
	
	public void openCircuit()  {
		if(!entryConnection.isConnected()) {
			System.out.println("Connecting...");
			entryConnection.connect();
		}
		circuitBuilder.build();
	}
	
	public void extendCircuit(RouterDescriptor router) {
		circuitBuilder.extendTo(router);
	}
	
	public Connection getConnection() {
		return entryConnection;
	}
	
	public int getCircuitId() {
		return circuitId;
	}
	
	public void sendRelayCell(RelayCell cell, CircuitNode targetNode) {
		System.out.println("Sending relay cell to --> "+ targetNode.getRouter().getNickname());
		cell.setLength();
		targetNode.updateForwardDigest(cell);
		cell.setDigest(targetNode.getForwardDigestBytes());

		for(CircuitNode node = targetNode; node != null; node = node.getPreviousNode())
			node.encryptForwardCell(cell);
		
		sendCell(cell);
	}
	
	public void sendRelayCellToFinalNode(RelayCell cell) {
		sendRelayCell(cell, getFinalCircuitNode());
	}
	
	public void sendCell(Cell cell) {
		try {
			entryConnection.sendCell(cell);
		} catch (ConnectionClosedException e) {
			e.printStackTrace();
		}
	}
	
	
	void appendNode(CircuitNode node) {
		circuitNodes.put(node.getRouter(), node);
		nodeList.add(node);
	}
	
	int getCircuitLength() {
		return nodeList.size();
	}
	
	CircuitNode getFinalCircuitNode() {
		if(nodeList.isEmpty())
			throw new TorException("getFinalCircuitNode() called on empty circuit");
		return nodeList.get( getCircuitLength() - 1);
		
	}
	
	public Cell receiveRelayCell(int expectedType) throws IOException {
		while(true) {
			final Cell cell = dequeueRelayCell();
			final CircuitNode node = decryptRelayCell(cell);
			if(node == null)
				throw new TorException("Node not found for received cell");
		
			final int command = cell.getByte();
			if(command == expectedType)
				return cell;
		
			processRelayCell(command, cell);
		}
		
	}
	
	private Cell dequeueRelayCell() {
		try {
			return relayCellQueue.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	private CircuitNode decryptRelayCell(Cell cell) {
		for(CircuitNode node: nodeList) {
			if(node.decryptBackwardCell(cell))
				return node;
		}
		return null;
		
	}
	
	Cell receiveControlCell() {
		try {
			return controlCellQueue.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	private void processRelayCell(int command, Cell cell) {
		switch(command) {
		case RelayCell.RELAY_TRUNCATED:
			System.out.println("recog: "+ cell.getShort()); // recognized
			System.out.println("stearm: "+ cell.getShort()); // stream
			System.out.println("digest: "+ cell.getInt()); // digest;
			System.out.println("length: "+ cell.getShort()); // length
			final int reason = cell.getByte();
			System.out.println("Truncated for reason: "+ reason);
			throw new TorException("Oh no");
		}
	}
	
	void queueControlCell(Cell cell) {
		controlCellQueue.add(cell);
	}
	
	void queueRelayCell(Cell cell) {
		relayCellQueue.add(cell);
	}
}
