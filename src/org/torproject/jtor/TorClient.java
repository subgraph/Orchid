package org.torproject.jtor;

import java.security.Security;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.torproject.jtor.circuits.TorInitializationTracker;
import org.torproject.jtor.dashboard.Dashboard;
import org.torproject.jtor.directory.downloader.DirectoryDownloader;
import org.torproject.jtor.socks.SocksPortListener;

/**
 * This class is the main entry-point for running a Tor proxy
 * or client.
 */
public class TorClient {
	private final static Logger logger = Logger.getLogger(TorClient.class.getName());
	private final TorConfig config;
	private final Directory directory;
	private final TorInitializationTracker initializationTracker;
	private final ConnectionCache connectionCache;
	private final CircuitManager circuitManager;
	private final SocksPortListener socksListener;
	private final DirectoryDownloader directoryDownloader;
	private final Dashboard dashboard;

	private boolean isStarted = false;
	private final CountDownLatch readyLatch;
	
	public TorClient() {
		Security.addProvider(new BouncyCastleProvider());
		config = Tor.createConfig();
		directory = Tor.createDirectory(config);
		initializationTracker = Tor.createInitalizationTracker();
		initializationTracker.addListener(createReadyFlagInitializationListener());
		connectionCache = Tor.createConnectionCache(initializationTracker);
		circuitManager = Tor.createCircuitManager(config, directory, connectionCache, initializationTracker);
		directoryDownloader = Tor.createDirectoryDownloader(directory, circuitManager);
		socksListener = Tor.createSocksPortListener(circuitManager);
		
		readyLatch = new CountDownLatch(1);
		
		dashboard = new Dashboard(); 
		dashboard.addRenderables(circuitManager, directoryDownloader, socksListener);
	}

	public TorConfig getConfig() {
		return config;
	}

	/**
	 * Start running the Tor client service.
	 */
	public synchronized void start() {
		if(isStarted) {
			return;
		}
		directoryDownloader.start();
		circuitManager.startBuildingCircuits();
		if(dashboard.isEnabledByProperty()) {
			dashboard.startListening();
		}
		isStarted = true;
	}
	
	public Directory getDirectory() {
		return directory;
	}
	
	public ConnectionCache getConnectionCache() {
		return connectionCache;
	}

	public CircuitManager getCircuitManager() {
		return circuitManager;
	}

	public void waitUntilReady() throws InterruptedException {
		readyLatch.await();
	}

	public void waitUntilReady(long timeout) throws InterruptedException, TimeoutException {
		if(!readyLatch.await(timeout, TimeUnit.MILLISECONDS)) {
			throw new TimeoutException();
		}
	}
	
	public Stream openExitStreamTo(String hostname, int port) throws InterruptedException, TimeoutException, OpenFailedException {
		ensureStarted();
		return circuitManager.openExitStreamTo(hostname, port);
	}
	
	private synchronized void ensureStarted() {
		if(!isStarted) {
			throw new IllegalStateException("Must call start() first");
		}
	}

	public void enableSocksListener(int port) {
		socksListener.addListeningPort(port);
	}

	public void enableSocksListener() {
		enableSocksListener(9150);
	}
	
	public void enableDashboard() {
		if(!dashboard.isListening()) {
			dashboard.startListening();
		}
	}
	
	public void enableDashboard(int port) {
		dashboard.setListeningPort(port);
		enableDashboard();
	}
	
	public void disableDashboard() {
		if(dashboard.isListening()) {
			dashboard.stopListening();
		}
	}

	public void addInitializationListener(TorInitializationListener listener) {
		initializationTracker.addListener(listener);
	}

	public void removeInitializationListener(TorInitializationListener listener) {
		initializationTracker.removeListener(listener);
	}
	
	private TorInitializationListener createReadyFlagInitializationListener() {
		return new TorInitializationListener() {
			public void initializationProgress(String message, int percent) {}
			public void initializationCompleted() {
				readyLatch.countDown();
			}
		};
	}

	public static void main(String[] args) {
		logger.info("Starting...");
		final TorClient client = new TorClient();
		client.addInitializationListener(createInitalizationListner());
		client.start();
		client.enableSocksListener();
	}
	
	private static TorInitializationListener createInitalizationListner() {
		return new TorInitializationListener() {
			
			public void initializationProgress(String message, int percent) {
				System.out.println(">>> [ "+ percent + "% ]: "+ message);
			}
			
			public void initializationCompleted() {
				System.out.println("Tor is ready to go!");
			}
		};
	}
}
