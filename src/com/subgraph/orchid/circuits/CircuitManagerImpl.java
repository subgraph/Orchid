package com.subgraph.orchid.circuits;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.subgraph.orchid.Circuit;
import com.subgraph.orchid.CircuitBuildHandler;
import com.subgraph.orchid.CircuitManager;
import com.subgraph.orchid.CircuitNode;
import com.subgraph.orchid.Connection;
import com.subgraph.orchid.ConnectionCache;
import com.subgraph.orchid.Directory;
import com.subgraph.orchid.OpenFailedException;
import com.subgraph.orchid.Router;
import com.subgraph.orchid.Stream;
import com.subgraph.orchid.StreamConnectFailedException;
import com.subgraph.orchid.Tor;
import com.subgraph.orchid.TorConfig;
import com.subgraph.orchid.circuits.guards.EntryGuards;
import com.subgraph.orchid.circuits.path.CircuitPathChooser;
import com.subgraph.orchid.crypto.TorRandom;
import com.subgraph.orchid.dashboard.DashboardRenderable;
import com.subgraph.orchid.dashboard.DashboardRenderer;
import com.subgraph.orchid.data.IPv4Address;

public class CircuitManagerImpl implements CircuitManager, DashboardRenderable {
	private final static int OPEN_DIRECTORY_STREAM_RETRY_COUNT = 5;
	private final static int OPEN_DIRECTORY_STREAM_TIMEOUT = 10 * 1000;
	
	interface CircuitFilter {
		boolean filter(CircuitBase circuit);
	}

	private final ConnectionCache connectionCache;
	private final Set<CircuitBase> activeCircuits;
	private final TorRandom random;
	private final PendingExitStreams pendingExitStreams;
	private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
	private final CircuitCreationTask circuitCreationTask;
	private final TorInitializationTracker initializationTracker;
	private final CircuitPathChooser pathChooser;

	public CircuitManagerImpl(TorConfig config, Directory directory, ConnectionCache connectionCache, TorInitializationTracker initializationTracker) {
		this.connectionCache = connectionCache;
		this.pathChooser = CircuitPathChooser.create(config, directory);
		if(config.getUseEntryGuards()) {
			this.pathChooser.enableEntryGuards(new EntryGuards(config, connectionCache, directory));
		}
		this.pendingExitStreams = new PendingExitStreams(config);
		this.circuitCreationTask = new CircuitCreationTask(config, directory, connectionCache, pathChooser, this, initializationTracker);
		this.activeCircuits = new HashSet<CircuitBase>();
		this.random = new TorRandom();
		
		this.initializationTracker = initializationTracker;
	}

	public void notifyInitializationEvent(int eventCode) {
		initializationTracker.notifyEvent(eventCode);
	}

	public void startBuildingCircuits() {
		scheduledExecutor.scheduleAtFixedRate(circuitCreationTask, 0, 1000, TimeUnit.MILLISECONDS);
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
				return !circuit.isDirectoryCircuit() && !circuit.isMarkedForClose() && circuit.isConnected();
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

	public Stream openExitStreamTo(String hostname, int port)
			throws InterruptedException, TimeoutException, OpenFailedException {
		circuitCreationTask.predictPort(port);
		return pendingExitStreams.openExitStream(hostname, port);
	}

	public Stream openExitStreamTo(IPv4Address address, int port)
			throws InterruptedException, TimeoutException, OpenFailedException {
		circuitCreationTask.predictPort(port);
		return pendingExitStreams.openExitStream(address, port);
	}

	public List<StreamExitRequest> getPendingExitStreams() {
		return pendingExitStreams.getUnreservedPendingRequests();
	}

	public Stream openDirectoryStream() throws OpenFailedException, InterruptedException, TimeoutException {
		return openDirectoryStream(0);
	}

	public Stream openDirectoryStream(int purpose) throws OpenFailedException, InterruptedException {
		final int requestEventCode = purposeToEventCode(purpose, false);
		final int loadingEventCode = purposeToEventCode(purpose, true);
		
		int failCount = 0;
		while(failCount < OPEN_DIRECTORY_STREAM_RETRY_COUNT) {
			final Circuit circuit = openDirectoryCircuit();
			if(requestEventCode > 0) {
				initializationTracker.notifyEvent(requestEventCode);
			}
			try {
				final Stream stream = circuit.openDirectoryStream(OPEN_DIRECTORY_STREAM_TIMEOUT);
				if(loadingEventCode > 0) {
					initializationTracker.notifyEvent(loadingEventCode);
				}
				return stream;
			} catch (StreamConnectFailedException e) {
				circuit.markForClose();
				failCount += 1;
			} catch (TimeoutException e) {
				circuit.markForClose();
			}
		}
		throw new OpenFailedException();
	}

	private CircuitBase openDirectoryCircuit() throws OpenFailedException {
		int failCount = 0;
		while(failCount < OPEN_DIRECTORY_STREAM_RETRY_COUNT) {
			final CircuitBase circuit = CircuitBase.createDirectoryCircuit(this);
			if(tryOpenDirectoryCircuit(circuit)) {
				return circuit;
			}
			failCount += 1;
		}
		throw new OpenFailedException("Could not create circuit for directory stream");
	}

	private boolean tryOpenDirectoryCircuit(CircuitBase circuit) {
		final DirectoryCircuitResult result = new DirectoryCircuitResult();
		final CircuitCreationRequest req = new CircuitCreationRequest(pathChooser, circuit, result);
		final CircuitBuildTask task = new CircuitBuildTask(req, connectionCache, initializationTracker);
		task.run();
		return result.isSuccessful();
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

		private boolean isFailed;
		
		public void connectionCompleted(Connection connection) {}
		public void nodeAdded(CircuitNode node) {}
		public void circuitBuildCompleted(Circuit circuit) {}
		
		public void connectionFailed(String reason) {
			isFailed = true;
		}

		public void circuitBuildFailed(String reason) {
			isFailed = true;
		}
		
		boolean isSuccessful() {
			return !isFailed;
		}
	}

	public void dashboardRender(DashboardRenderer renderer, PrintWriter writer, int flags) throws IOException {
		if((flags & DASHBOARD_CIRCUITS) == 0) {
			return;
		}
		renderer.renderComponent(writer, flags, connectionCache);
		renderer.renderComponent(writer, flags, circuitCreationTask.getCircuitPredictor());
		writer.println("[Circuit Manager]");
		writer.println();
		for(Circuit c: getCircuitsByFilter(null)) {
			renderer.renderComponent(writer, flags, c);
		}
	}
}
