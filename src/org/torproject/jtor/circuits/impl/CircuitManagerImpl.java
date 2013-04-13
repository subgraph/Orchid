package org.torproject.jtor.circuits.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.torproject.jtor.Tor;
import org.torproject.jtor.TorConfig;
import org.torproject.jtor.circuits.Circuit;
import org.torproject.jtor.circuits.CircuitBuildHandler;
import org.torproject.jtor.circuits.CircuitManager;
import org.torproject.jtor.circuits.CircuitNode;
import org.torproject.jtor.circuits.Connection;
import org.torproject.jtor.circuits.OpenStreamResponse;
import org.torproject.jtor.circuits.OpenStreamResponse.OpenStreamStatus;
import org.torproject.jtor.circuits.guards.EntryGuards;
import org.torproject.jtor.circuits.path.CircuitPathChooser;
import org.torproject.jtor.connections.ConnectionCache;
import org.torproject.jtor.crypto.TorRandom;
import org.torproject.jtor.data.IPv4Address;
import org.torproject.jtor.directory.Directory;
import org.torproject.jtor.directory.Router;

public class CircuitManagerImpl implements CircuitManager {
	
	private final static Logger logger = Logger.getLogger(CircuitManagerImpl.class.getName());
	private final static boolean DEBUG_CIRCUIT_CREATION = true;
	private final static boolean USE_ENTRY_GUARDS = true;
	
	interface CircuitFilter {
		boolean filter(CircuitBase circuit);
	}

	private final TorConfig config;
	private final ConnectionCache connectionCache;
	private final Set<CircuitBase> activeCircuits;
	private final TorRandom random;
	private final List<StreamExitRequest> pendingExitStreams = new LinkedList<StreamExitRequest>();
	private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
	private final CircuitCreationTask circuitCreationTask;
	private final TorInitializationTracker initializationTracker;
	private final CircuitPathChooser pathChooser;

	public CircuitManagerImpl(TorConfig config, Directory directory, ConnectionCache connectionCache, TorInitializationTracker initializationTracker) {
		this.config = config;
		this.connectionCache = connectionCache;
		this.pathChooser = new CircuitPathChooser(directory);
		if(USE_ENTRY_GUARDS) {
			this.pathChooser.enableEntryGuards(new EntryGuards(connectionCache, directory));
		}
		
		this.circuitCreationTask = new CircuitCreationTask(directory, connectionCache, pathChooser, this, initializationTracker);
		this.activeCircuits = new HashSet<CircuitBase>();
		this.random = new TorRandom();
		this.initializationTracker = initializationTracker;
	}

	public void notifyInitializationEvent(int eventCode) {
		initializationTracker.notifyEvent(eventCode);
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
			logger.fine("CLEAN: "+ getCleanCircuitCount() 
					+ " PENDING: "+ getPendingCircuitCount()
					+ " ACTIVE: "+ getActiveCircuitCount());
		}};
	}

	public ExitCircuit createNewExitCircuit(Router exitRouter) {
		return CircuitBase.create(this, exitRouter);
	}

	void addActiveCircuit(CircuitBase circuit) {
		synchronized (activeCircuits) {
			activeCircuits.add(circuit);
		}
	}
	
	void removeActiveCircuit(CircuitBase circuit) {
		synchronized (activeCircuits) {
			activeCircuits.remove(circuit);
		}
	}

	Set<Circuit> getCleanCircuits() {
		final Set<Circuit> result = new HashSet<Circuit>();
		synchronized(activeCircuits) {
			for(CircuitBase c: activeCircuits) {
				if(c.isClean() && !c.isDirectoryCircuit()) {
					result.add(c);
				}
			}
		}
		return result;
	}
	
	synchronized int getCleanCircuitCount() {
		return getCleanCircuits().size();
	}

	synchronized int getActiveCircuitCount() {
		return activeCircuits.size();
	}

	Set<Circuit> getPendingCircuits() {
		return getCircuitsByFilter(new CircuitFilter() {
			public boolean filter(CircuitBase circuit) {
				return circuit.isPending();
			}
		});
	}

	synchronized int getPendingCircuitCount() {
		return getPendingCircuits().size();
	}
	
	Set<Circuit> getCircuitsByFilter(CircuitFilter filter) {
		final Set<Circuit> result = new HashSet<Circuit>();
		synchronized (activeCircuits) {
			for(CircuitBase c: activeCircuits) {
				if(filter == null || filter.filter(c)) {
					result.add(c);
				}
			}
		}
		return result;
	}

	List<Circuit> getRandomlyOrderedListOfActiveCircuits() {
		final Set<Circuit> notDirectory = getCircuitsByFilter(new CircuitFilter() {
			
			public boolean filter(CircuitBase circuit) {
				return !circuit.isDirectoryCircuit() && circuit.isConnected();
			}
		});
		final ArrayList<Circuit> ac = new ArrayList<Circuit>(notDirectory);
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
			circuitCreationTask.predictPort(request.getPort());
			pendingExitStreams.add(request);
			while(!request.isCompleted()) {
				pendingExitStreams.wait();
			}
		}
		return request.getResponse();
	}

	public List<StreamExitRequest> getPendingExitStreams() {
		synchronized(pendingExitStreams) {
			return new ArrayList<StreamExitRequest>(pendingExitStreams);
		}
	}
	
	void streamRequestIsCompleted(StreamExitRequest request) {
		synchronized(pendingExitStreams) {
			pendingExitStreams.remove(request);
			pendingExitStreams.notifyAll();
		}
	}

	public OpenStreamResponse openDirectoryStream() {
		return openDirectoryStream(0);
	}

	public OpenStreamResponse openDirectoryStream(int purpose) {
		final int requestEventCode = purposeToEventCode(purpose, false);
		final int loadingEventCode = purposeToEventCode(purpose, true);
		
		final CircuitBase circuit = CircuitBase.createDirectoryCircuit(this);
		final DirectoryCircuitResult result = new DirectoryCircuitResult();
		final CircuitCreationRequest req = new CircuitCreationRequest(pathChooser, circuit, result);
		final CircuitBuildTask task = new CircuitBuildTask(req, connectionCache, initializationTracker);
		task.run();

		
		if(result.isSuccessful()) {
			if(requestEventCode > 0) {
				initializationTracker.notifyEvent(requestEventCode);
			}
			final OpenStreamResponse osr =  circuit.openDirectoryStream();
			if(osr.getStatus() == OpenStreamStatus.STATUS_STREAM_OPENED && loadingEventCode > 0) {
				initializationTracker.notifyEvent(loadingEventCode);
			}
			return osr;
		} else {
			return OpenStreamResponseImpl.createConnectionFailError(result.getErrorMessage());
		}
	}

	private int purposeToEventCode(int purpose, boolean getLoadingEvent) {
		switch(purpose) {
		case DIRECTORY_PURPOSE_CONSENSUS:
			return getLoadingEvent ? Tor.BOOTSTRAP_STATUS_LOADING_STATUS : Tor.BOOTSTRAP_STATUS_REQUESTING_STATUS;
		case DIRECTORY_PURPOSE_CERTIFICATES:
			 return getLoadingEvent ? Tor.BOOTSTRAP_STATUS_LOADING_KEYS : Tor.BOOTSTRAP_STATUS_REQUESTING_KEYS;
		case DIRECTORY_PURPOSE_DESCRIPTORS:
			return getLoadingEvent ? Tor.BOOTSTRAP_STATUS_LOADING_DESCRIPTORS : Tor.BOOTSTRAP_STATUS_REQUESTING_DESCRIPTORS;
		default:
			return 0;
		}
	}

	private static class DirectoryCircuitResult implements CircuitBuildHandler {

		private String errorMessage;
		private boolean isFailed;
		
		public void connectionCompleted(Connection connection) {}
		public void nodeAdded(CircuitNode node) {}
		public void circuitBuildCompleted(Circuit circuit) {}
		
		public void connectionFailed(String reason) {
			errorMessage = reason;
			isFailed = true;
		}

		public void circuitBuildFailed(String reason) {
			errorMessage = reason;
			isFailed = true;
		}
		
		boolean isSuccessful() {
			return !isFailed;
		}
		
		String getErrorMessage() {
			return errorMessage;
		}
	}

	public void dashboardRender(PrintWriter writer, int flags) throws IOException {
		connectionCache.dashboardRender(writer, flags);
		circuitCreationTask.getCircuitPredictor().dashboardRender(writer, flags);
		writer.println("[Circuit Manager]");
		writer.println();
		for(Circuit c: getCircuitsByFilter(null)) {
			c.dashboardRender(writer, flags);
		}
		writer.println();
	}
}
