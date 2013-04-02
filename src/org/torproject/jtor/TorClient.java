package org.torproject.jtor;

import java.security.Security;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.torproject.jtor.circuits.CircuitManager;
import org.torproject.jtor.circuits.OpenStreamResponse;
import org.torproject.jtor.circuits.impl.TorInitializationTracker;
import org.torproject.jtor.dashboard.Dashboard;
import org.torproject.jtor.directory.Directory;
import org.torproject.jtor.directory.downloader.DirectoryDownloader;
import org.torproject.jtor.socks.SocksPortListener;

/**
 * This class is the main entry-point for running a Tor proxy
 * or client.
 */
public class TorClient {
	private final TorConfig config;
	private final Directory directory;
	private final TorInitializationTracker initializationTracker;
	private final CircuitManager circuitManager;
	private final SocksPortListener socksListener;
	private final DirectoryDownloader directoryDownloader;
	private final Dashboard dashboard;

	private boolean isStarted = false;
	private volatile boolean isReady = false;
	private Object readyLock = new Object();
	
	public TorClient() {
		Security.addProvider(new BouncyCastleProvider());
		config = Tor.createConfig();
		directory = Tor.createDirectory(config);
		initializationTracker = Tor.createInitalizationTracker();
		initializationTracker.addListener(createReadyFlagInitializationListener());
		circuitManager = Tor.createCircuitManager(directory, initializationTracker);
		directoryDownloader = Tor.createDirectoryDownloader(directory, circuitManager);
		socksListener = Tor.createSocksPortListener(circuitManager);
		
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
		directory.loadFromStore();
		directoryDownloader.start();
		circuitManager.startBuildingCircuits();
		isStarted = true;
	}
	
	public void waitUntilReady() throws InterruptedException {
		waitUntilReady(0);
	}

	public void waitUntilReady(long timeout) throws InterruptedException {
		synchronized (readyLock) {
			while(!isReady) {
				readyLock.wait(timeout);
			}
		}
	}

	public OpenStreamResponse openExitStreamTo(String hostname, int port) throws InterruptedException {
		if(!isStarted) {
			throw new IllegalStateException("Must call start() first");
		}
		return circuitManager.openExitStreamTo(hostname, port);
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

	void addInitializationListener(TorInitializationListener listener) {
		initializationTracker.addListener(listener);
	}

	void removeInitializationListener(TorInitializationListener listener) {
		initializationTracker.removeListener(listener);
	}
	
	private TorInitializationListener createReadyFlagInitializationListener() {
		return new TorInitializationListener() {
			public void initializationProgress(String message, int percent) {}
			public void initializationCompleted() {
				synchronized (readyLock) {
					isReady = true;
					readyLock.notifyAll();
				}
			}
		};
	}

	public static void main(String[] args) {
		setupLogging();
		final TorClient client = new TorClient();
		
		client.addInitializationListener(createInitalizationListner());
		client.start();
		client.enableSocksListener();
		client.enableDashboard();
	}
	
	public static void setupLogging() {
		System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tT] %4$s: %5$s%6$s%n");
		setLogHandlerLevel(Level.FINE);
		//Logger.getLogger("org.torproject.jtor.circuits").setLevel(Level.FINE);
	}
	
	private static void setLogHandlerLevel(Level level) {
		for(Handler handler: Logger.getLogger("").getHandlers()) {
			handler.setLevel(level);
		}
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
