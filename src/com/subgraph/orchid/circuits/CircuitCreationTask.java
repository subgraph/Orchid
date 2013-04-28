package com.subgraph.orchid.circuits;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.subgraph.orchid.Circuit;
import com.subgraph.orchid.CircuitBuildHandler;
import com.subgraph.orchid.CircuitNode;
import com.subgraph.orchid.Connection;
import com.subgraph.orchid.ConnectionCache;
import com.subgraph.orchid.Directory;
import com.subgraph.orchid.Router;
import com.subgraph.orchid.TorConfig;
import com.subgraph.orchid.circuits.CircuitManagerImpl.CircuitFilter;
import com.subgraph.orchid.circuits.path.CircuitPathChooser;
import com.subgraph.orchid.data.exitpolicy.ExitTarget;

public class CircuitCreationTask implements Runnable {
	private final static Logger logger = Logger.getLogger(CircuitCreationTask.class.getName());
	private final static int MAX_CIRCUIT_DIRTINESS = 300; // seconds
	private final static int MAX_PENDING_CIRCUITS = 4;

	private final TorConfig config;
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
	
	private Date lastNewCircuit;

	CircuitCreationTask(TorConfig config, Directory directory, ConnectionCache connectionCache, CircuitPathChooser pathChooser, CircuitManagerImpl circuitManager, TorInitializationTracker initializationTracker) {
		this.config = config;
		this.directory = directory;
		this.connectionCache = connectionCache;
		this.circuitManager = circuitManager;
		this.initializationTracker = initializationTracker;
		this.pathChooser = pathChooser;
		this.executor = Executors.newCachedThreadPool();
		this.buildHandler = createCircuitBuildHandler();
		this.predictor = new CircuitPredictor();
	}

	CircuitPredictor getCircuitPredictor() {
		return predictor;
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

			public boolean filter(CircuitBase circuit) {
				return !circuit.isMarkedForClose() && circuit.getSecondsDirty() > MAX_CIRCUIT_DIRTINESS;
			}
		});
		for(Circuit c: circuits) {
			logger.fine("Closing idle dirty circuit: "+ c);
			((CircuitBase)c).markForClose();
		}
	}
	private void checkExpiredPendingCircuits() {
		// TODO Auto-generated method stub
	}

	private void checkCircuitsForCreation() {

		if(!directory.haveMinimumRouterInfo()) {
			if(notEnoughDirectoryInformationWarningCounter % 20 == 0)
				logger.info("Cannot build circuits because we don't have enough directory information");
			notEnoughDirectoryInformationWarningCounter++;
			return;
		}

		if(lastNewCircuit != null) {
			final Date now = new Date();
			if((now.getTime() - lastNewCircuit.getTime()) < config.getNewCircuitPeriod()) {
				return;
			}
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
		lastNewCircuit = new Date();
	}

	private int countCircuitsSupportingTarget(final ExitTarget target, final boolean needClean) {
		final CircuitFilter filter = new CircuitFilter() {
			public boolean filter(CircuitBase circuit) {
				final boolean pendingOrConnected = circuit.isPending() || circuit.isConnected();
				final boolean isCleanIfNeeded = !(needClean && !circuit.isClean());
				return pendingOrConnected && isCleanIfNeeded && circuit.canHandleExitTo(target);
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
		final Router exitRouter = pathChooser.chooseExitNodeForTargets(exitTargets);
		if(exitRouter == null) {
			logger.warning("Failed to select suitable exit node for targets");
			return;
		}
		
		final CircuitBase circuit = circuitManager.createNewExitCircuit(exitRouter);
		final CircuitCreationRequest request = new CircuitCreationRequest(pathChooser, circuit, buildHandler);
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
