package org.torproject.jtor.circuits.impl;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.torproject.jtor.Logger;
import org.torproject.jtor.TorException;
import org.torproject.jtor.circuits.Circuit;
import org.torproject.jtor.circuits.CircuitManager;
import org.torproject.jtor.directory.Directory;

public class CircuitManagerImpl implements CircuitManager {
	private final static boolean DEBUG_CIRCUIT_CREATION = true;

	private final ConnectionManagerImpl connectionManager;
	private final Logger logger;
	private final Set<Circuit> pendingCircuits;
	private final Set<Circuit> activeCircuits;
	private final Set<Circuit> cleanCircuits;
	private final SecureRandom random;
	private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
	private final Runnable circuitCreationTask;

	public CircuitManagerImpl(Directory directory, ConnectionManagerImpl connectionManager, StreamManagerImpl streamManager, Logger logger) {
		this.connectionManager = connectionManager;
		this.circuitCreationTask = new CircuitCreationTask(directory, streamManager, this, logger);
		this.logger = logger;
		this.activeCircuits = new HashSet<Circuit>();
		this.pendingCircuits = new HashSet<Circuit>();
		this.cleanCircuits = new HashSet<Circuit>();
		this.random = createRandom();
	}

	private static SecureRandom createRandom() {
		try {
			return SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			throw new TorException(e);
		}
	}

	public void startBuildingCircuits() {
		scheduledExecutor.scheduleAtFixedRate(circuitCreationTask, 0, 1000, TimeUnit.MILLISECONDS);

		if(DEBUG_CIRCUIT_CREATION) {
			Runnable debugTask = createCircuitCreationDebugTask();
			scheduledExecutor.scheduleAtFixedRate(debugTask, 0, 10000, TimeUnit.MILLISECONDS);
		}
	}

	private Runnable createCircuitCreationDebugTask() {
		return new Runnable() { public void run() {
			logger.debug("CLEAN: "+ getCleanCircuitCount() 
					+ " PENDING: "+ getPendingCircuitCount()
					+ " ACTIVE: "+ getActiveCircuitCount());
		}};
	}

	public Circuit newCircuit() {
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

	synchronized void circuitClosed(Circuit circuit) {
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
}
