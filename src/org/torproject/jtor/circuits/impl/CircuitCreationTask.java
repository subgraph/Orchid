package org.torproject.jtor.circuits.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.torproject.jtor.circuits.Circuit;
import org.torproject.jtor.circuits.CircuitBuildHandler;
import org.torproject.jtor.circuits.CircuitNode;
import org.torproject.jtor.circuits.Connection;
import org.torproject.jtor.circuits.impl.CircuitManagerImpl.CircuitFilter;
import org.torproject.jtor.circuits.path.CircuitPathChooser;
import org.torproject.jtor.connections.ConnectionCache;
import org.torproject.jtor.data.exitpolicy.ExitTarget;
import org.torproject.jtor.directory.Directory;

public class CircuitCreationTask implements Runnable {
	private final static Logger logger = Logger.getLogger(CircuitCreationTask.class.getName());
	private final static int MAX_CIRCUIT_DIRTINESS = 300; // seconds
	private final static int MAX_PENDING_CIRCUITS = 4;

	private final Directory directory;
	private final ConnectionCache connectionCache;
	private final CircuitManagerImpl circuitManager;
	private final TorInitializationTracker initializationTracker;
	private final CircuitPathChooser pathChooser;
	private final Executor executor;
	private final CircuitBuildHandler buildHandler;
	// To avoid obnoxiously printing a warning every second
	private int notEnoughDirectoryInformationWarningCounter = 0;
	
	private final CircuitPredictor predictor;

	CircuitCreationTask(Directory directory, ConnectionCache connectionCache, CircuitPathChooser pathChooser, CircuitManagerImpl circuitManager, TorInitializationTracker initializationTracker) {
		this.directory = directory;
		this.connectionCache = connectionCache;
		this.circuitManager = circuitManager;
		this.initializationTracker = initializationTracker;
		this.pathChooser = pathChooser;
		this.executor = Executors.newCachedThreadPool();
		this.buildHandler = createCircuitBuildHandler();
		this.predictor = new CircuitPredictor();
	}

	public void run() {
		expireOldCircuits();
		assignPendingStreamsToActiveCircuits();
		checkExpiredPendingCircuits();
		checkCircuitsForCreation();		
	}

	void predictPort(int port) {
		predictor.addExitPortRequest(port);
	}

	private void assignPendingStreamsToActiveCircuits() {
		final List<StreamExitRequest> pendingExitStreams = circuitManager.getPendingExitStreams();
		if(pendingExitStreams.isEmpty())
			return;

		for(Circuit c: circuitManager.getRandomlyOrderedListOfActiveCircuits()) {
			final Iterator<StreamExitRequest> it = pendingExitStreams.iterator();
			while(it.hasNext()) {
				if(attemptHandleStreamRequest(c, it.next()))
					it.remove();
			}
		}
	}

	private boolean attemptHandleStreamRequest(Circuit c, StreamExitRequest request) {
		if(c.canHandleExitTo(request)) {
			if(request.reserveRequest())
				executor.execute(newExitStreamTask(c, request));
			// else request is reserved meaning another circuit is already trying to handle it
			return true;
		}
		return false;
	}

	private OpenExitStreamTask newExitStreamTask(Circuit circuit, StreamExitRequest exitRequest) {
		return new OpenExitStreamTask(circuit, exitRequest);
	}

	private void expireOldCircuits() {
		final Set<Circuit> circuits = circuitManager.getCircuitsByFilter(new CircuitFilter() {

			public boolean filter(CircuitImpl circuit) {
				return !circuit.isMarkedForClose() && circuit.getSecondsDirty() > MAX_CIRCUIT_DIRTINESS;
			}
		});
		for(Circuit c: circuits) {
			logger.fine("Closing idle dirty circuit: "+ c);
			((CircuitImpl)c).markForClose();
		}
	}
	private void checkExpiredPendingCircuits() {
		// TODO Auto-generated method stub
	}

	private void checkCircuitsForCreation() {

		if(!directory.haveMinimumRouterInfo()) {
			if(notEnoughDirectoryInformationWarningCounter % 20 == 0)
				logger.warning("Cannot build circuits because we don't have enough directory information");
			notEnoughDirectoryInformationWarningCounter++;
			return;
		}

		final List<StreamExitRequest> pendingExitStreams = circuitManager.getPendingExitStreams();
		final List<PredictedPortTarget> predictedPorts = predictor.getPredictedPortTargets();
		final List<ExitTarget> exitTargets = new ArrayList<ExitTarget>();
		for(StreamExitRequest streamRequest: pendingExitStreams) {
			if(!streamRequest.isReserved() && countCircuitsSupportingTarget(streamRequest, false) == 0) {
				exitTargets.add(streamRequest);
			}
		}
		for(PredictedPortTarget ppt: predictedPorts) {
			if(countCircuitsSupportingTarget(ppt, true) < 2) {
				exitTargets.add(ppt);
			}
		}
		
		buildCircuitToHandleExitTargets(exitTargets);
	}

	private int countCircuitsSupportingTarget(final ExitTarget target, final boolean needClean) {
		final CircuitFilter filter = new CircuitFilter() {
			public boolean filter(CircuitImpl circuit) {
				final boolean notDirectory = !circuit.isDirectoryCircuit();
				final boolean pendingOrConnected = circuit.isPending() || circuit.isConnected();
				final boolean isCleanIfNeeded = !(needClean && !circuit.isClean());
				return notDirectory && pendingOrConnected && isCleanIfNeeded && circuit.canHandleExitTo(target);
			}
		};
		return circuitManager.getCircuitsByFilter(filter).size();
	}

	private void buildCircuitToHandleExitTargets(List<ExitTarget> exitTargets) {
		if(exitTargets.isEmpty()) {
			return;
		}
		if(!directory.haveMinimumRouterInfo())
			return;
		if(circuitManager.getPendingCircuitCount() >= MAX_PENDING_CIRCUITS)
			return;

		if(logger.isLoggable(Level.FINE)) { 
			logger.fine("Building new circuit to handle "+ exitTargets.size() +" pending streams and predicted ports");
		}

		launchBuildTaskForTargets(exitTargets);
	}

	private void launchBuildTaskForTargets(List<ExitTarget> exitTargets) {
		final CircuitImpl circuit = circuitManager.createNewCircuit();
		final CircuitCreationRequest request = new CircuitCreationRequest(pathChooser, circuit, exitTargets, buildHandler, false);
		final CircuitBuildTask task = new  CircuitBuildTask(request, connectionCache, initializationTracker);
		executor.execute(task);
	}

	private CircuitBuildHandler createCircuitBuildHandler() {
		return new CircuitBuildHandler() {

			public void circuitBuildCompleted(Circuit circuit) {
				logger.fine("Circuit completed to: "+ circuit);
				circuitOpenedHandler(circuit);
			}

			public void circuitBuildFailed(String reason) {
				logger.fine("Circuit build failed: "+ reason);
			}

			public void connectionCompleted(Connection connection) {
				logger.finer("Circuit connection completed to "+ connection);
			}

			public void connectionFailed(String reason) {
				logger.fine("Circuit connection failed: "+ reason);
			}

			public void nodeAdded(CircuitNode node) {
				logger.finer("Node added to circuit: "+ node);
			}	
		};
	}

	private void circuitOpenedHandler(Circuit circuit) {
		final List<StreamExitRequest> pendingExitStreams = circuitManager.getPendingExitStreams();
		for(StreamExitRequest req: pendingExitStreams) {
			if(circuit.canHandleExitTo(req) && req.reserveRequest())
				executor.execute(newExitStreamTask(circuit, req));
		}
	}

}
