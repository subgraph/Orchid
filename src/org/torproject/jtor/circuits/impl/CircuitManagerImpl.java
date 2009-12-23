package org.torproject.jtor.circuits.impl;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.torproject.jtor.Logger;
import org.torproject.jtor.TorException;
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
	private final StreamManagerImpl streamManager;
	private final Set<Circuit> pendingCircuits;
	private final Set<Circuit> activeCircuits;
	private final Set<Circuit> cleanCircuits;
	private final Thread circuitCreationThread;
	private final NodeChooser nodeChooser;
	private final SecureRandom random;

	public CircuitManagerImpl(Directory directory, ConnectionManagerImpl connectionManager, StreamManagerImpl streamManager, Logger logger) {
		this.directory = directory;
		this.connectionManager = connectionManager;
		this.streamManager = streamManager;
		this.logger = logger;
		this.activeCircuits = new HashSet<Circuit>();
		this.pendingCircuits = new HashSet<Circuit>();
		this.cleanCircuits = new HashSet<Circuit>();
		this.nodeChooser = new NodeChooser(streamManager, directory);
		this.circuitCreationThread = createCircuitCreationThread();
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
		circuitCreationThread.start();
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

	public Stream getDirectoryStream() {
		
		// TODO Auto-generated method stub
		return null;
	}

	private Thread createCircuitCreationThread() {
		return new Thread(new Runnable() {
			public void run() {
				while(true) {
					try {
						checkUnassignedPendingStreams();
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

	private void checkUnassignedPendingStreams() {
		final List<StreamExitRequest> pendingExitStreams = streamManager.getPendingExitStreams();
		if(pendingExitStreams.isEmpty())
			return;

		final List<Circuit> circuits = getRandomlyOrderedListOfActiveCircuits();
		for(Circuit c : circuits) {
			for(StreamExitRequest req : pendingExitStreams) {
				final Router lastRouter = c.getFinalCircuitNode().getRouter();
				if(canRouterHandleExitRequest(lastRouter, req) && req.reserveRequest()) {
					asynchTryOpenExitStream(c, req);
				}
			}
		}
	}

	private List<Circuit> getRandomlyOrderedListOfActiveCircuits() {
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
	private void checkExpiredPendingCircuits() {

	}
	/* called every second */
	private void checkCircuitsForCreation() {
		//logger.debug("clean circuits: "+ cleanCircuits.size() + " pending circuits: "+ pendingCircuits.size() + " active circuits: "+ activeCircuits.size());
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
		logger.debug("Opening new preemptive circuit");
		final List<Router> path = choosePreemptiveExitPath();
		final Circuit circuit = newCircuit();
		synchronized(pendingCircuits) {
			pendingCircuits.add(circuit);
		}
		circuit.openCircuit(path, new CircuitBuildHandler() {

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

		});

	}

	private void preemptiveCircuitOpened(Circuit circuit) {
		final Router lastRouter = circuit.getFinalCircuitNode().getRouter();
		final List<StreamExitRequest> pendingExitStreams = streamManager.getPendingExitStreams();
		for(StreamExitRequest req: pendingExitStreams) {
			if(canRouterHandleExitRequest(lastRouter, req) && req.reserveRequest()) 
				tryOpenExitStream(circuit, req);
		}
	}

	private boolean canRouterHandleExitRequest(Router router, StreamExitRequest request) {
		if(request.isAddressRequest())
			return router.exitPolicyAccepts(request.getAddress(), request.getPort());
		else
			return router.exitPolicyAccepts(request.getPort());
	}

	private void asynchTryOpenExitStream(final Circuit circuit, final StreamExitRequest req) {
		final Thread t = new Thread(new Runnable() { public void run() {
			tryOpenExitStream(circuit, req);
			}
		});
		t.run();
	}

	private void tryOpenExitStream(Circuit circuit, StreamExitRequest req) {
		logger.debug("Attempting to open stream to "+ req);
		final Stream stream = doOpen(circuit, req);
		if(stream == null) {
			req.unreserveRequest();
			return;
		}
		req.setAllocatedStream(stream);
		streamManager.streamIsConnected(req);
	}

	private Stream doOpen(Circuit circuit, StreamExitRequest req) {
		if(req.isAddressRequest())
			return circuit.openExitStream(req.getAddress(), req.getPort());
		else
			return circuit.openExitStream(req.getHostname(), req.getPort());
	}

}
