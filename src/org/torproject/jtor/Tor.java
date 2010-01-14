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

/**
 * The <code>Tor</code> class is a collection of static methods for instantiating
 * various subsystem modules.
 */
public class Tor {
	private final static String version = "JTor 0.0.0";
	
	/**
	 * Return a string describing the version of this software.
	 * 
	 * @return A string representation of the software version.
	 */
	public static String getVersion() {
		return version;
	}
	
	/**
	 * Create and return a new <code>LogManager</code> instance.
	 * 
	 * @return A new <code>LogManager</code>
	 * @see LogManager
	 */
	static public LogManager createLogManager() {
		return new LogManagerImpl();
	}

	/**
	 * Create and return a new <code>TorConfig</code> instance.
	 * 
	 * @param logManager This is a required dependency.  You must create a <code>LogManager</code>
	 *                   before calling this method to create a <code>TorConfig</code>
	 * @return A new <code>TorConfig</code> instance.
	 * @see TorConfig
	 */
	static public TorConfig createConfig(LogManager logManager) {
		return new TorConfigImpl(logManager);
	}

	/**
	 * Create and return a new <code>Directory</code> instance.
	 * 
	 * @param logManager This is a required dependency.  You must create a <code>LogManager</code> 
	 *                   before creating a <code>Directory</code>. 
	 * @param config This is a required dependency. You must create a <code>TorConfig</code> before
	 *               calling this method to create a <code>Directory</code>
	 * @return A new <code>Directory</code> instance.
	 * @see Directory
	 */
	static public Directory createDirectory(LogManager logManager, TorConfig config) {
		return new DirectoryImpl(logManager, config);
	}

	/**
	 * Create and return a new <code>CircuitManager</code> instance.
	 * 
	 * @param directory This is a required dependency.  You must create a <code>Directory</code> 
	 *                  before calling this method to create a <code>CircuitManager</code>.
	 * @param logManager This is a required dependency.  You must create a <code>LogManager</code>
	 *                   before calling this method to create a <code>CircuitManager</code>.
	 * @return A new <code>CircuitManager</code> instance.
	 * @see CircuitManager
	 */
	static public CircuitManager createCircuitManager(Directory directory, LogManager logManager) {
		final ConnectionManagerImpl connectionManager = new ConnectionManagerImpl(logManager);
		return new CircuitManagerImpl(directory, connectionManager, logManager);
	}

	/**
	 * Create and return a new <code>SocksPortListener</code> instance.
	 * 
	 * @param logManager This is a required dependency.  You must create a <code>LogManager</code>
	 *                   before calling this method to create a <code>SocksPortListener</code>.
	 * @param circuitManager This is a required dependency.  You must create a <code>CircuitManager</code>
	 *                       before calling this method to create a <code>SocksPortListener</code>.
	 * @return A new <code>SocksPortListener</code> instance.
	 * @see SocksPortListener
	 */
	static public SocksPortListener createSocksPortListener(LogManager logManager, CircuitManager circuitManager) {
		return new SocksPortListenerImpl(logManager, circuitManager);
	}

	/**
	 * Create and return a new <code>NetworkStatusManager</code> instance.
	 * 
	 * @param directory This is a required dependency.  You must create a <code>Directory</code>
	 *                  before calling this method to create a <code>NetworkStatusManager</code>
	 * @param logManager This is a required dependency.  You must create a <code>LogManager</code>
	 *                   before calling this method to create a <code>NetworkStatusManager</code>.
	 * @return A new <code>NetworkStatusManager</code> instance.
	 * @see NetworkStatusManager
	 */
	static public NetworkStatusManager createNetworkStatusManager(Directory directory, LogManager logManager) {
		return new NetworkStatusManager(directory, logManager);
	}
}
