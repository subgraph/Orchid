package org.torproject.jtor.circuits.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.torproject.jtor.Logger;
import org.torproject.jtor.circuits.Circuit;
import org.torproject.jtor.circuits.CircuitBuildHandler;
import org.torproject.jtor.circuits.CircuitManager;
import org.torproject.jtor.circuits.CircuitNode;
import org.torproject.jtor.circuits.Connection;
import org.torproject.jtor.circuits.Stream;
import org.torproject.jtor.directory.Directory;
import org.torproject.jtor.directory.Router;

public class CircuitManagerImpl implements CircuitManager {

	private final static int MAX_PENDING_CIRCUITS = 2;
	private final static int DEFAULT_CLEAN_CIRCUITS = 3;
	private final Directory directory;
	private final ConnectionManagerImpl connectionManager;
	private final Logger logger;
	private final StreamManager streamManager;
	private final Set<Circuit> pendingCircuits;
	private final Set<Circuit> activeCircuits;
	private final Set<Circuit> cleanCircuits;
	private final Thread circuitCreationThread;
	private final NodeChooser nodeChooser;

	public CircuitManagerImpl(Directory directory, ConnectionManagerImpl connectionManager, StreamManager streamManager, Logger logger) {
		this.directory = directory;
		this.connectionManager = connectionManager;
		this.streamManager = streamManager;
		this.logger = logger;
		this.activeCircuits = new HashSet<Circuit>();
		this.pendingCircuits = new HashSet<Circuit>();
		this.cleanCircuits = new HashSet<Circuit>();
		this.nodeChooser = new NodeChooser(streamManager, directory);
		this.circuitCreationThread = createCircuitCreationThread();
	}

	public void startBuildingCircuits() {
		circuitCreationThread.start();
	}
	public Circuit createCircuitFromNicknames(List<String> nicknamePath) {
		final List<Router> path = directory.getRouterListByNames(nicknamePath);
		return createCircuitFromPath(path);
	}

	public Circuit createCircuitFromPath(List<Router> path) {
		return CircuitImpl.create(this, connectionManager, path);
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

	public Stream getDirectoryStream() {
		
		// TODO Auto-generated method stub
		return null;
	}

	private Thread createCircuitCreationThread() {
		return new Thread(new Runnable() {
			public void run() {
				while(true) {
					try {
						checkExpiredPendingCircuits();
						checkCircuitsForCreation();
					
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						return;
					} catch (Exception e) {
						logger.debug("Something bad happened: "+ e);
						e.printStackTrace();
					}
				}
			}
			
		});
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

	private void checkExpiredPendingCircuits() {
		
	}
	/* called every second */
	private void checkCircuitsForCreation() {
		if((cleanCircuits.size() + pendingCircuits.size()) < DEFAULT_CLEAN_CIRCUITS && 
				pendingCircuits.size() < MAX_PENDING_CIRCUITS) {
			final Thread t = createLaunchCircuitThread();
			t.start();
		}
	}

	private Thread createLaunchCircuitThread() {
		return new Thread(new Runnable() {
			public void run() {
				launchNewPreemptiveCircuit();				
			}	
		});
	}

	private void launchNewPreemptiveCircuit() {
		if(!directory.haveMinimumRouterInfo()) {
			logger.warn("Cannot build circuits because we don't have enough directory information");
			return;
		}

		final List<Router> path = choosePreemptiveExitPath();
		final Circuit circuit = createCircuitFromPath(path);
		synchronized(pendingCircuits) {
			pendingCircuits.add(circuit);
		}
		circuit.openCircuit(new CircuitBuildHandler() {

			public void circuitBuildCompleted(Circuit circuit) {
				logger.debug("Preemptive Circuit completed to: "+ circuit);
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
				logger.debug("Node added to circuit: "+ node);				
			}
			
		});
		
	}

}
