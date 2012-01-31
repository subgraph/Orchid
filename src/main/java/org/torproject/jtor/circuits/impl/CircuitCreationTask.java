package org.torproject.jtor.circuits.impl;

import java.util.Arrays;
import java.util.Iterator;
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

		System.out.println("Building new circuits to handle "+ pendingExitStreams.size() +" pending streams");
		for(StreamExitRequest r: pendingExitStreams) {
			System.out.println("Request: "+ r);

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
		return new OpenExitStreamTask(circuit, exitRequest, logger);
	}

	private void checkExpiredPendingCircuits() {
		// TODO Auto-generated method stub
	}

	private void createCircuitForExitRequest(StreamExitRequest request) {
		if(!directory.haveMinimumRouterInfo())
			return;
		if(circuitManager.getPendingCircuitCount() >= MAX_PENDING_CIRCUITS)
			return;

		final Circuit circuit = circuitManager.createNewCircuit();
		final NodeChoiceConstraints ncc = new NodeChoiceConstraints();
		// XXX
		ncc.setNeedCapacity(true);
		ncc.setNeedUptime(true);
		final Router exitRouter = nodeChooser.chooseExitNodeForTarget(request, ncc);
		ncc.addExcludedRouter(exitRouter);
		final Router middleRouter = nodeChooser.chooseMiddleNode(ncc);
		ncc.addExcludedRouter(middleRouter);
		final Router entryRouter = nodeChooser.chooseEntryNode(ncc);
		List<Router> path =  Arrays.asList(entryRouter, middleRouter, exitRouter);
		executor.execute(new OpenCircuitTask(circuit, path, createCircuitBuildHandler(), logger));
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
				logger.debug("Circuit completed to: "+ circuit);
				circuitOpenedHandler(circuit);
			}

			public void circuitBuildFailed(String reason) {
				logger.debug("Circuit build failed: "+ reason);
			}

			public void connectionCompleted(Connection connection) {
				logger.debug("Circuit connection completed to "+ connection);
			}

			public void connectionFailed(String reason) {
				logger.debug("Circuit connection failed: "+ reason);
			}

			public void nodeAdded(CircuitNode node) {
				//logger.debug("Node added to circuit: "+ node);
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
