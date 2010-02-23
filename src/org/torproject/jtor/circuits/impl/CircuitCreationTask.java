package org.torproject.jtor.circuits.impl;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.torproject.jtor.circuits.Circuit;
import org.torproject.jtor.circuits.CircuitBuildHandler;
import org.torproject.jtor.circuits.CircuitNode;
import org.torproject.jtor.circuits.Connection;
import org.torproject.jtor.directory.Directory;
import org.torproject.jtor.directory.Router;
import org.torproject.jtor.logging.Logger;

public class CircuitCreationTask implements Runnable {
	private final static int MAX_PENDING_CIRCUITS = 2;
	private final static int DEFAULT_CLEAN_CIRCUITS = 3;
	private final Directory directory;
	private final CircuitManagerImpl circuitManager;
	private final Logger logger;
	private final NodeChooser nodeChooser;
	private final Executor executor;
	
	// To avoid obnoxiously printing a warning every second
	private int notEnoughDirectoryInformationWarningCounter = 0;
	
	CircuitCreationTask(Directory directory, CircuitManagerImpl circuitManager, Logger logger) {
		this.directory = directory;
		this.circuitManager = circuitManager;
		this.logger = logger;
		this.nodeChooser = new NodeChooser(circuitManager, directory);
		this.executor = Executors.newCachedThreadPool();
	}
	
	public void run() {
		checkUnassignedPendingStreams();
		checkExpiredPendingCircuits();
		checkCircuitsForCreation();		
	}

	private void checkUnassignedPendingStreams() {
		final List<StreamExitRequest> pendingExitStreams = circuitManager.getPendingExitStreams();
		if(pendingExitStreams.isEmpty())
			return;

		final List<Circuit> circuits = circuitManager.getRandomlyOrderedListOfActiveCircuits();
		for(Circuit c : circuits) {
			for(StreamExitRequest req : pendingExitStreams) {
				final Router lastRouter = c.getFinalCircuitNode().getRouter();
				if(canRouterHandleExitRequest(lastRouter, req) && req.reserveRequest()) {
					executor.execute(newExitStreamTask(c, req));
				}
			}
		}
		
	}

	private OpenExitStreamTask newExitStreamTask(Circuit circuit, StreamExitRequest exitRequest) {
		return new OpenExitStreamTask(circuit, exitRequest, logger);
	}
	
	private boolean canRouterHandleExitRequest(Router router, StreamExitRequest request) {
		if(request.isAddressRequest())
			return router.exitPolicyAccepts(request.getAddress(), request.getPort());
		else
			return router.exitPolicyAccepts(request.getPort());
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
		
		if((circuitManager.getCleanCircuitCount() + circuitManager.getPendingCircuitCount()) < DEFAULT_CLEAN_CIRCUITS &&
				circuitManager.getPendingCircuitCount() < MAX_PENDING_CIRCUITS) {
			final List<Router> path = choosePreemptiveExitPath();
			final Circuit circuit = circuitManager.createNewCircuit();
			executor.execute(new OpenCircuitTask(circuit, path, createCircuitBuildHandler(), logger));
		}
		
	}
		
	private List<Router> choosePreemptiveExitPath() {
		final NodeChoiceConstraints ncc = new NodeChoiceConstraints();
		ncc.setNeedCapacity(true);
		ncc.setNeedUptime(true);
		final Router exitRouter = nodeChooser.chooseExitNodeForPort(80, ncc);
		ncc.addExcludedRouter(exitRouter);
		final Router middleRouter = nodeChooser.chooseMiddleNode(ncc);
		ncc.addExcludedRouter(middleRouter);
		final Router entryRouter = nodeChooser.chooseEntryNode(ncc);
		return Arrays.asList(entryRouter, middleRouter, exitRouter);
	}
	
	private CircuitBuildHandler createCircuitBuildHandler() {
		return new CircuitBuildHandler() {

			public void circuitBuildCompleted(Circuit circuit) {
				logger.debug("Preemptive Circuit completed to: "+ circuit);
				preemptiveCircuitOpened(circuit);				
			}

			public void circuitBuildFailed(String reason) {
				logger.debug("Preemptive circuit build failed: "+ reason);				
			}

			public void connectionCompleted(Connection connection) {
				logger.debug("Preemptive circuit connection completed to "+ connection);				
			}

			public void connectionFailed(String reason) {
				logger.debug("Preemptive circuit connection failed: "+ reason);				
			}

			public void nodeAdded(CircuitNode node) {
				//logger.debug("Node added to circuit: "+ node);				
			}	
		};
	}
	
	private void preemptiveCircuitOpened(Circuit circuit) {
		final Router lastRouter = circuit.getFinalCircuitNode().getRouter();
		final List<StreamExitRequest> pendingExitStreams = circuitManager.getPendingExitStreams();
		for(StreamExitRequest req: pendingExitStreams) {
			if(canRouterHandleExitRequest(lastRouter, req) && req.reserveRequest())
				executor.execute(newExitStreamTask(circuit, req));
		}
	}
}
