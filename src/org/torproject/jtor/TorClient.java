package org.torproject.jtor;

import java.security.Security;

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
		final TorClient client = new TorClient();
		client.start();
		client.enableSocksListener();
	}
}
