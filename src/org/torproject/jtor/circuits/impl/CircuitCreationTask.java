package org.torproject.jtor.circuits.impl;

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
import org.torproject.jtor.connections.ConnectionCache;
import org.torproject.jtor.directory.Directory;
import org.torproject.jtor.directory.Router;

public class CircuitCreationTask implements Runnable {
	private final static Logger logger = Logger.getLogger(CircuitCreationTask.class.getName());
	private final static int MAX_CIRCUIT_DIRTINESS = 300; // seconds
	private final static int MAX_PENDING_CIRCUITS = 2;
	private final static int DEFAULT_CLEAN_CIRCUITS = 3;
	private final Directory directory;
	private final ConnectionCache connectionCache;
	private final CircuitManagerImpl circuitManager;
	private final TorInitializationTracker initializationTracker;
	private final CircuitPathChooser nodeChooser;
	private final Executor executor;
	private final CircuitBuildHandler buildHandler;
	// To avoid obnoxiously printing a warning every second
	private int notEnoughDirectoryInformationWarningCounter = 0;
	
	private final CircuitPredictor predictor;

	CircuitCreationTask(Directory directory, ConnectionCache connectionCache, CircuitManagerImpl circuitManager, TorInitializationTracker initializationTracker) {
		this.directory = directory;
		this.connectionCache = connectionCache;
		this.circuitManager = circuitManager;
		this.initializationTracker = initializationTracker;
		this.nodeChooser = new CircuitPathChooser(circuitManager, directory);
		this.executor = Executors.newCachedThreadPool();
		this.buildHandler = createCircuitBuildHandler();
		this.predictor = new CircuitPredictor(nodeChooser, circuitManager);
	}

	public void run() {
		expireOldCircuits();
		checkUnassignedPendingStreams();
		checkExpiredPendingCircuits();
		checkCircuitsForCreation();		
	}

	CircuitPredictor getCircuitPredictor() {
		return predictor;
	}

	private void checkUnassignedPendingStreams() {
		final List<StreamExitRequest> pendingExitStreams = circuitManager.getPendingExitStreams();

		assignPendingStreamsToActiveCircuits(pendingExitStreams);

		removePendingStreamsByPendingCircuits(pendingExitStreams);

		buildCircuitsToHandleExitStreams(pendingExitStreams);
	}

	private void assignPendingStreamsToActiveCircuits(List<StreamExitRequest> pendingExitStreams) {
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
	
	private void removePendingStreamsByPendingCircuits(List<StreamExitRequest> pendingExitStreams) {
		if(pendingExitStreams.isEmpty())
			return;

		for(Circuit c: circuitManager.getPendingCircuits()) {
			final Iterator<StreamExitRequest> it = pendingExitStreams.iterator();
			while(it.hasNext()) {
				if(c.canHandleExitTo(it.next()))
					it.remove();
			}
		}
	}

	private void buildCircuitsToHandleExitStreams(List<StreamExitRequest> pendingExitStreams) {
		if(pendingExitStreams.isEmpty())
			return;

		if(logger.isLoggable(Level.FINE)) {
			logger.fine("Building new circuits to handle "+ pendingExitStreams.size() +" pending streams");
			for(StreamExitRequest r: pendingExitStreams) {
				logger.fine("Request: "+ r);
			}
		}

		for(StreamExitRequest req: pendingExitStreams)
			createCircuitForExitRequest(req);
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

	private void createCircuitForExitRequest(StreamExitRequest request) {
		if(!directory.haveMinimumRouterInfo())
			return;
		if(circuitManager.getPendingCircuitCount() >= MAX_PENDING_CIRCUITS)
			return;

		final List<Router> path = nodeChooser.choosePathForTarget(request);
		launchBuildTaskForPath(path);
	}

	private void checkCircuitsForCreation() {

		if(!directory.haveMinimumRouterInfo()) {
			if(notEnoughDirectoryInformationWarningCounter % 20 == 0)
				logger.warning("Cannot build circuits because we don't have enough directory information");
			notEnoughDirectoryInformationWarningCounter++;
			return;
		}

		if((circuitManager.getCleanCircuitCount() + circuitManager.getPendingCircuitCount()) < DEFAULT_CLEAN_CIRCUITS &&
				circuitManager.getPendingCircuitCount() < MAX_PENDING_CIRCUITS) {
			Set<Circuit> circuits = circuitManager.getCircuitsByFilter(new CircuitFilter() {
				
				public boolean filter(CircuitImpl circuit) {
					return !circuit.isDirectoryCircuit() && (circuit.isClean() || circuit.isPending());
				}
			});
			final List<CircuitCreationRequest> predictedCircuitRequests = predictor.generatePredictedCircuitRequests(circuits, buildHandler);
			for(CircuitCreationRequest req: predictedCircuitRequests) {
				executor.execute(new CircuitBuildTask(req, connectionCache, initializationTracker));
			}
		}
	}


	private void launchBuildTaskForPath(List<Router> path) {
		final CircuitImpl circuit = circuitManager.createNewCircuit();
		final CircuitCreationRequest request = new CircuitCreationRequest(circuit, path, buildHandler, false);
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
				logger.fine("Circuit connection completed to "+ connection);
			}

			public void connectionFailed(String reason) {
				logger.fine("Circuit connection failed: "+ reason);
			}

			public void nodeAdded(CircuitNode node) {
				logger.fine("Node added to circuit: "+ node);
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
