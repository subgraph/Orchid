package org.torproject.jtor;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.torproject.jtor.circuits.CircuitManager;
import org.torproject.jtor.directory.Directory;
import org.torproject.jtor.directory.impl.NetworkStatusManager;
import org.torproject.jtor.logging.LogManager;
import org.torproject.jtor.socks.SocksPortListener;

/**
 * This class is the main entry-point for running a Tor proxy
 * or client.
 */
public class TorClient {
	private final LogManager logManager;
	private final TorConfig config;
	private final Directory directory;
	private final CircuitManager circuitManager;
	private final SocksPortListener socksListener;
	private final NetworkStatusManager networkStatusManager;

	public TorClient() {
		Security.addProvider(new BouncyCastleProvider());
		logManager = Tor.createLogManager();
		config = Tor.createConfig(logManager);
		directory = Tor.createDirectory(logManager, config);
		circuitManager = Tor.createCircuitManager(directory, logManager);
		networkStatusManager = Tor.createNetworkStatusManager(directory, logManager);
		socksListener = Tor.createSocksPortListener(logManager, circuitManager);
		
	}

	/**
	 * Start running the Tor client service.
	 */
	public void start() {
		directory.loadFromStore();
		networkStatusManager.startDownloadingDocuments();
		circuitManager.startBuildingCircuits();
		socksListener.addListeningPort(5090);
	}

	public static void main(String[] args) {
		final TorClient client = new TorClient();
		client.start();
	}
}
