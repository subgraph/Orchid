package org.torproject.jtor.circuits.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.torproject.jtor.data.IPv4Address;
import org.torproject.jtor.data.exitpolicy.ExitTarget;
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
	private final Set<ExitTarget> failedExitRequests;
	private final CircuitBuilder circuitBuilder;
	private final CircuitStatus status;
	private final Object relaySendLock = new Object();
	
	private CircuitImpl(CircuitManagerImpl circuitManager, ConnectionManagerImpl connectionManager, Logger logger) {
		nodeList = new ArrayList<CircuitNodeImpl>();
		this.circuitManager = circuitManager;
		this.logger = logger;
		this.relayCellResponseQueue = new LinkedBlockingQueue<RelayCell>();
		this.controlCellResponseQueue = new LinkedBlockingQueue<Cell>();
		this.streamMap = new HashMap<Integer, StreamImpl>();
		this.failedExitRequests = new HashSet<ExitTarget>();
		status = new CircuitStatus();
		circuitBuilder = new CircuitBuilder(this, connectionManager, logger);
	}

	void initializeConnectingCircuit(ConnectionImpl entryConnection, int circuitId) {
		this.circuitId = circuitId;
		this.entryConnection = entryConnection;
	}

	public boolean isConnected() {
		return status.isConnected();
	}

	void setConnected() {
		status.setStateConnected();
	}

	public void openCircuit(List<Router> circuitPath, CircuitBuildHandler handler)  {
		startCircuitOpen(circuitPath);	
		if(circuitBuilder.openCircuit(circuitPath, handler)) 
			circuitOpenSucceeded();
		else
			circuitOpenFailed();
	}

	private void startCircuitOpen(List<Router> circuitPath) {
		if(!status.isUnconnected())
			throw new IllegalStateException("Can only connect UNCONNECTED circuits");
		status.updateCreatedTimestamp();
		status.setStateBuilding(circuitPath);
		circuitManager.circuitStartConnect(this);
	}

	private void circuitOpenFailed() {
		status.setStateFailed();
		circuitManager.circuitInactive(this);
	}

	private void circuitOpenSucceeded() {
		status.setStateOpen();
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

			if(cell.getRelayCommand() == RelayCell.RELAY_DATA) 
				targetNode.waitForSendWindowAndDecrement();
			
			sendCell(cell);
		}
	}

	public void sendCell(Cell cell) {
		if(!(status.isConnected() || status.isBuilding()))
			return;
		try {
			status.updateDirtyTimestamp();
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
		if(status.isBuilding())
			return remainingBuildTime();
		else
			return CIRCUIT_RELAY_RESPONSE_TIMEOUT;
	}

	private long remainingBuildTime() {
		final long elapsed = status.getMillisecondsElapsedSinceCreated();
		if(elapsed == 0 || elapsed >= CIRCUIT_BUILD_TIMEOUT_MS)
			return 0;
		return CIRCUIT_BUILD_TIMEOUT_MS - elapsed;
	}

	/*
	 * This is called by the cell reading thread in ConnectionImpl to deliver control cells 
	 * associated with this circuit (CREATED or CREATED_FAST).
	 */
	void deliverControlCell(Cell cell) {
		status.updateDirtyTimestamp();
		controlCellResponseQueue.add(cell);
	}

	/* This is called by the cell reading thread in ConnectionImpl to deliver RELAY cells. */
	void deliverRelayCell(Cell cell) {
		status.updateDirtyTimestamp();
		final RelayCell relayCell = decryptRelayCell(cell);
		logger.debug("Dispatching: "+ relayCell);
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
				sendRelayCell(sendme);
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
				logger.debug("Stream not found for stream id="+ cell.getStreamId());	
		}
	}

	private void processCircuitSendme(RelayCell cell) {
		cell.getCircuitNode().incrementSendWindow();
	}

	public OpenStreamResponse openDirectoryStream() {
		return null;
	}

	public OpenStreamResponse openExitStream(IPv4Address address, int port) {
		return openExitStream(address.toString(), port);
	}

	public OpenStreamResponse openExitStream(String target, int port) {
		final StreamImpl stream = createNewStream();
		final OpenStreamResponse response = stream.openExit(target, port);
		switch(response.getStatus()) {
		case STATUS_STREAM_TIMEOUT:
			logger.info("Timeout opening stream: "+ stream);
			if(status.countStreamTimeout()) {
				// XXX do something
			}
			removeStream(stream);
			break;

		case STATUS_STREAM_ERROR:
			logger.info("Error opening stream: "+ stream +" reason: "+ response.getErrorCodeMessage());
			removeStream(stream);
			break;

		}
		return response;
	}

	boolean isFinalNodeDirectory() {
		return getFinalCircuitNode().getRouter().getDirectoryPort() != 0;
	}

	void destroyCircuit() {
		status.setStateDestroyed();
		entryConnection.removeCircuit(this);
		synchronized(streamMap) {
			final List<StreamImpl> tmpList = new ArrayList<StreamImpl>(streamMap.values());
			for(StreamImpl s: tmpList)
				s.close();
		}
		circuitManager.circuitInactive(this);
	}

	private StreamImpl createNewStream() {
		synchronized(streamMap) {
			final int streamId = status.nextStreamId();
			final StreamImpl stream = new StreamImpl(this, getFinalCircuitNode(), streamId);
			streamMap.put(streamId, stream);
			return stream;
		}
	}

	void removeStream(StreamImpl stream) {
		synchronized(streamMap) {
			streamMap.remove(stream.getStreamId());
		}
	}

	public void recordFailedExitTarget(ExitTarget target) {
		synchronized(failedExitRequests) {
			failedExitRequests.add(target);
		}
	}

	public boolean canHandleExitTo(ExitTarget target) {
		synchronized(failedExitRequests) {
			if(failedExitRequests.contains(target))
				return false;
		}

		final Router lastRouter = status.getFinalRouter();
		if(target.isAddressTarget())
			return lastRouter.exitPolicyAccepts(target.getAddress(), target.getPort());
		else
			return lastRouter.exitPolicyAccepts(target.getPort());
	}

	public String toString() {
		return "Circuit id="+ circuitId +" state=" + status.getStateAsString() +" "+ pathToString();
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
