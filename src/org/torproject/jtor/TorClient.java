package org.torproject.jtor;

import java.security.Security;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.torproject.jtor.circuits.CircuitManager;
import org.torproject.jtor.circuits.OpenStreamResponse;
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
	private final CircuitManager circuitManager;
	private final SocksPortListener socksListener;
	private final DirectoryDownloader directoryDownloader;

	private boolean isStarted = false;
	
	public TorClient() {
		Security.addProvider(new BouncyCastleProvider());
		config = Tor.createConfig();
		directory = Tor.createDirectory(config);
		circuitManager = Tor.createCircuitManager(directory);
		directoryDownloader = Tor.createDirectoryDownloader(directory, circuitManager);
		socksListener = Tor.createSocksPortListener(circuitManager);
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
		enableSocksListener(5090);
	}

	public static void main(String[] args) {
		setupLogging();
		final TorClient client = new TorClient();
		client.circuitManager.addInitializationListener(createInitalizationListner());
		client.start();
		client.enableSocksListener();
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
