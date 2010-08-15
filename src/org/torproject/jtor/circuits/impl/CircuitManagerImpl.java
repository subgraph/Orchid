package org.torproject.jtor.circuits.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.torproject.jtor.circuits.Circuit;
import org.torproject.jtor.circuits.CircuitManager;
import org.torproject.jtor.circuits.OpenStreamResponse;
import org.torproject.jtor.crypto.TorRandom;
import org.torproject.jtor.data.IPv4Address;
import org.torproject.jtor.directory.Directory;
import org.torproject.jtor.logging.LogManager;
import org.torproject.jtor.logging.Logger;

public class CircuitManagerImpl implements CircuitManager {
	private final static boolean DEBUG_CIRCUIT_CREATION = true;

	private final ConnectionManagerImpl connectionManager;
	private final Logger logger;
	private final Set<Circuit> pendingCircuits;
	private final Set<Circuit> activeCircuits;
	private final Set<Circuit> cleanCircuits;
	private final TorRandom random;
	private final List<StreamExitRequest> pendingExitStreams = new LinkedList<StreamExitRequest>();
	private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
	private final Runnable circuitCreationTask;

	public CircuitManagerImpl(Directory directory, ConnectionManagerImpl connectionManager, LogManager logManager) {
		this.connectionManager = connectionManager;
		this.logger = logManager.getLogger("circuits");
		this.logger.enableDebug();
		this.circuitCreationTask = new CircuitCreationTask(directory, this, logger);
		this.activeCircuits = new HashSet<Circuit>();
		this.pendingCircuits = new HashSet<Circuit>();
		this.cleanCircuits = new HashSet<Circuit>();
		this.random = new TorRandom();
	}

	public void startBuildingCircuits() {
		scheduledExecutor.scheduleAtFixedRate(circuitCreationTask, 0, 1000, TimeUnit.MILLISECONDS);

		if(DEBUG_CIRCUIT_CREATION) {
			Runnable debugTask = createCircuitCreationDebugTask();
			scheduledExecutor.scheduleAtFixedRate(debugTask, 0, 30000, TimeUnit.MILLISECONDS);
		}
	}

	private Runnable createCircuitCreationDebugTask() {
		return new Runnable() { public void run() {
			logger.debug("CLEAN: "+ getCleanCircuitCount() 
					+ " PENDING: "+ getPendingCircuitCount()
					+ " ACTIVE: "+ getActiveCircuitCount());
		}};
	}

	public Circuit createNewCircuit() {
		return CircuitImpl.create(this, connectionManager, logger);
	}

	synchronized void circuitStartConnect(Circuit circuit) {
		pendingCircuits.add(circuit);
	}

	synchronized void circuitConnected(Circuit circuit) {
		pendingCircuits.remove(circuit);
		activeCircuits.add(circuit);
		cleanCircuits.add(circuit);
	}

	synchronized void circuitDirty(Circuit circuit) {
		cleanCircuits.remove(circuit);
	}

	synchronized void circuitInactive(Circuit circuit) {
		pendingCircuits.remove(circuit);
		activeCircuits.remove(circuit);
		cleanCircuits.remove(circuit);
	}

	synchronized int getCleanCircuitCount() {
		return cleanCircuits.size();
	}

	synchronized int getActiveCircuitCount() {
		return activeCircuits.size();
	}

	synchronized int getPendingCircuitCount() {
		return pendingCircuits.size();
	}

	List<Circuit> getRandomlyOrderedListOfActiveCircuits() {
		final ArrayList<Circuit> ac = new ArrayList<Circuit>(activeCircuits);
		final int sz = ac.size();
		for(int i = 0; i < sz; i++) {
			final Circuit tmp = ac.get(i);
			final int swapIdx = random.nextInt(sz);
			ac.set(i, ac.get(swapIdx));
			ac.set(swapIdx, tmp);
		}
		return ac;
	}

	public OpenStreamResponse openExitStreamTo(String hostname, int port)
			throws InterruptedException {
		return openExitStreamByRequest(new StreamExitRequest(this, hostname, port));
	}

	public OpenStreamResponse openExitStreamTo(IPv4Address address, int port)
			throws InterruptedException {
		return openExitStreamByRequest(new StreamExitRequest(this, address, port));
	}
	
	private OpenStreamResponse openExitStreamByRequest(StreamExitRequest request) throws InterruptedException {
		synchronized(pendingExitStreams) {
			pendingExitStreams.add(request);
			while(!request.isCompleted())
				pendingExitStreams.wait();
		}
		return request.getResponse();
	}
	
	List<StreamExitRequest> getPendingExitStreams() {
		synchronized(pendingExitStreams) {
			return new ArrayList<StreamExitRequest>(pendingExitStreams);
		}
	}
	
	List<Circuit> getPendingCircuits() {
		synchronized(pendingCircuits) {
			return new ArrayList<Circuit>(pendingCircuits);
		}
	}
	
	void streamRequestIsCompleted(StreamExitRequest request) {
		synchronized(pendingExitStreams) {
			pendingExitStreams.remove(request);
			pendingExitStreams.notifyAll();
		}
	}
}
