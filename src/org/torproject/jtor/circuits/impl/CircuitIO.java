package org.torproject.jtor.circuits.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.torproject.jtor.TorException;
import org.torproject.jtor.circuits.CircuitNode;
import org.torproject.jtor.circuits.Connection;
import org.torproject.jtor.circuits.ConnectionIOException;
import org.torproject.jtor.circuits.cells.Cell;
import org.torproject.jtor.circuits.cells.RelayCell;

public class CircuitIO {
	private static final Logger logger = Logger.getLogger(CircuitIO.class.getName());
	private final static long CIRCUIT_BUILD_TIMEOUT_MS = 30 * 1000;
	private final static long CIRCUIT_RELAY_RESPONSE_TIMEOUT = 20 * 1000;

	private final CircuitImpl circuit;
	private final Connection connection;
	private final int circuitId;
	
	private final BlockingQueue<RelayCell> relayCellResponseQueue;
	private final BlockingQueue<Cell> controlCellResponseQueue;
	private final Map<Integer, StreamImpl> streamMap;
	private final Object relaySendLock = new Object();
	
	CircuitIO(CircuitImpl circuit, Connection connection, int circuitId) {
		this.circuit = circuit;
		this.connection = connection;
		this.circuitId = circuitId;
		
		this.relayCellResponseQueue = new LinkedBlockingQueue<RelayCell>();
		this.controlCellResponseQueue = new LinkedBlockingQueue<Cell>();
		this.streamMap = new HashMap<Integer, StreamImpl>();
	}
	
	Connection getConnection() {
		return connection;
	}
	
	int getCircuitId() {
		return circuitId;
	}

	RelayCell dequeueRelayResponseCell() {
		try {
			final long timeout = getReceiveTimeout();
			return relayCellResponseQueue.poll(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return null;
		}
	}

	private RelayCell decryptRelayCell(Cell cell) {
		for(CircuitNodeImpl node: circuit.getNodeList()) {
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
		if(circuit.getStatus().isBuilding())
			return remainingBuildTime();
		else
			return CIRCUIT_RELAY_RESPONSE_TIMEOUT;
	}

	private long remainingBuildTime() {
		final long elapsed = circuit.getStatus().getMillisecondsElapsedSinceCreated();
		if(elapsed == 0 || elapsed >= CIRCUIT_BUILD_TIMEOUT_MS)
			return 0;
		return CIRCUIT_BUILD_TIMEOUT_MS - elapsed;
	}

	/*
	 * This is called by the cell reading thread in ConnectionImpl to deliver control cells 
	 * associated with this circuit (CREATED or CREATED_FAST).
	 */
	void deliverControlCell(Cell cell) {
		circuit.getStatus().updateDirtyTimestamp();
		controlCellResponseQueue.add(cell);
	}

	/* This is called by the cell reading thread in ConnectionImpl to deliver RELAY cells. */
	void deliverRelayCell(Cell cell) {
		circuit.getStatus().updateDirtyTimestamp();
		final RelayCell relayCell = decryptRelayCell(cell);
		if(relayCell.getRelayCommand() != RelayCell.RELAY_DATA)
			logger.fine("Dispatching: "+ relayCell);
		switch(relayCell.getRelayCommand()) {
		case RelayCell.RELAY_EXTENDED:
		case RelayCell.RELAY_RESOLVED:
		case RelayCell.RELAY_TRUNCATED:
			relayCellResponseQueue.add(relayCell);
			break;	
		case RelayCell.RELAY_DATA:
		case RelayCell.RELAY_END:
		case RelayCell.RELAY_CONNECTED:
			processRelayDataCell(relayCell);
			break;

		case RelayCell.RELAY_SENDME:
			if(relayCell.getStreamId() != 0)
				processRelayDataCell(relayCell);
			else
				processCircuitSendme(relayCell);
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
		if(cell.getRelayCommand() == RelayCell.RELAY_DATA) {
			cell.getCircuitNode().decrementDeliverWindow();
			if(cell.getCircuitNode().considerSendingSendme()) {
				final RelayCell sendme = createRelayCell(RelayCell.RELAY_SENDME, 0, cell.getCircuitNode());
				sendRelayCellTo(sendme, sendme.getCircuitNode());
			}
		}

		synchronized(streamMap) {
			final StreamImpl stream = streamMap.get(cell.getStreamId());
			// It's not unusual for the stream to not be found.  For example, if a RELAY_CONNECTED arrives after
			// the client has stopped waiting for it, the stream will never be tracked and eventually the edge node
			// will send a RELAY_END for this stream.
			if(stream != null)
				stream.addInputCell(cell);
			else
				logger.fine("Stream not found for stream id="+ cell.getStreamId());	
		}
	}
	
	RelayCell createRelayCell(int relayCommand, int streamId, CircuitNode targetNode) {
		return new RelayCellImpl(targetNode, circuitId, streamId, relayCommand);
	}

	void sendRelayCellTo(RelayCell cell, CircuitNode targetNode) {
		synchronized(relaySendLock) {
			logger.fine("Sending:     "+ cell);
			cell.setLength();
			targetNode.updateForwardDigest(cell);
			cell.setDigest(targetNode.getForwardDigestBytes());

			for(CircuitNode node = targetNode; node != null; node = node.getPreviousNode())
				node.encryptForwardCell(cell);

			if(cell.getRelayCommand() == RelayCell.RELAY_DATA) 
				targetNode.waitForSendWindowAndDecrement();
			
			sendCell(cell);
		}
	}
	
	void sendCell(Cell cell) {
		final CircuitStatus status = circuit.getStatus();
		if(!(status.isConnected() || status.isBuilding()))
			return;
		try {
			status.updateDirtyTimestamp();
			connection.sendCell(cell);
		} catch (ConnectionIOException e) {
			destroyCircuit();
		}
	}

	private void processCircuitSendme(RelayCell cell) {
		cell.getCircuitNode().incrementSendWindow();
	}

	void destroyCircuit() {
		circuit.getStatus().setStateDestroyed();
		connection.removeCircuit(circuit);
		synchronized(streamMap) {
			final List<StreamImpl> tmpList = new ArrayList<StreamImpl>(streamMap.values());
			for(StreamImpl s: tmpList)
				s.close();
		}
	}
	
	StreamImpl createNewStream() {
		synchronized(streamMap) {
			final int streamId = circuit.getStatus().nextStreamId();
			final StreamImpl stream = new StreamImpl(circuit, circuit.getFinalCircuitNode(), streamId);
			streamMap.put(streamId, stream);
			return stream;
		}
	}

	void removeStream(StreamImpl stream) {
		synchronized(streamMap) {
			streamMap.remove(stream.getStreamId());
		}
	}
}
