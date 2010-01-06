package org.torproject.jtor;

import org.torproject.jtor.circuits.CircuitManager;
import org.torproject.jtor.circuits.impl.CircuitManagerImpl;
import org.torproject.jtor.circuits.impl.ConnectionManagerImpl;
import org.torproject.jtor.config.impl.TorConfigImpl;
import org.torproject.jtor.directory.Directory;
import org.torproject.jtor.directory.impl.DirectoryImpl;
import org.torproject.jtor.directory.impl.NetworkStatusManager;
import org.torproject.jtor.logging.LogManager;
import org.torproject.jtor.logging.impl.LogManagerImpl;
import org.torproject.jtor.socks.SocksPortListener;
import org.torproject.jtor.socks.impl.SocksPortListenerImpl;

public class Tor {
	private final static String version = "JTor 0.0.0";
	
	public static String getVersion() {
		return version;
	}
	
	static public LogManager createLogManager() {
		return new LogManagerImpl();
	}

	static public TorConfig createConfig(LogManager logManager) {
		return new TorConfigImpl(logManager);
	}

	static public Directory createDirectory(LogManager logManager, TorConfig config) {
		return new DirectoryImpl(logManager, config);
	}

	static public CircuitManager createCircuitManager(Directory directory, LogManager logManager) {
		final ConnectionManagerImpl connectionManager = new ConnectionManagerImpl(logManager);
		return new CircuitManagerImpl(directory, connectionManager, logManager);
	}

	static public SocksPortListener createSocksPortListener(LogManager logManager, CircuitManager circuitManager) {
		return new SocksPortListenerImpl(logManager, circuitManager);
	}

	static public NetworkStatusManager createNetworkStatusManager(Directory directory, LogManager logManager) {
		return new NetworkStatusManager(directory, logManager);
	}
}
