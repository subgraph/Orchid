package com.subgraph.orchid;

import java.lang.reflect.Proxy;

import com.subgraph.orchid.circuits.CircuitManagerImpl;
import com.subgraph.orchid.circuits.TorInitializationTracker;
import com.subgraph.orchid.config.TorConfigProxy;
import com.subgraph.orchid.connections.ConnectionCacheImpl;
import com.subgraph.orchid.directory.DirectoryImpl;
import com.subgraph.orchid.directory.downloader.DirectoryDownloader;
import com.subgraph.orchid.socks.SocksPortListenerImpl;

/**
 * The <code>Tor</code> class is a collection of static methods for instantiating
 * various subsystem modules.
 */
public class Tor {
	
	public final static int BOOTSTRAP_STATUS_STARTING = 0;
	public final static int BOOTSTRAP_STATUS_CONN_DIR = 5;
	public final static int BOOTSTRAP_STATUS_HANDSHAKE_DIR = 10;
	public final static int BOOTSTRAP_STATUS_ONEHOP_CREATE = 15;
	public final static int BOOTSTRAP_STATUS_REQUESTING_STATUS = 20;
	public final static int BOOTSTRAP_STATUS_LOADING_STATUS = 25;
	public final static int BOOTSTRAP_STATUS_REQUESTING_KEYS = 35;
	public final static int BOOTSTRAP_STATUS_LOADING_KEYS = 40;
	public final static int BOOTSTRAP_STATUS_REQUESTING_DESCRIPTORS = 45;
	public final static int BOOTSTRAP_STATUS_LOADING_DESCRIPTORS = 50;
	public final static int BOOTSTRAP_STATUS_CONN_OR = 80;
	public final static int BOOTSTRAP_STATUS_HANDSHAKE_OR = 85;
	public final static int BOOTSTRAP_STATUS_CIRCUIT_CREATE = 90;
	public final static int BOOTSTRAP_STATUS_DONE = 100;
	
	
	private final static String implementation = "Orchid";
	private final static String version = "0.9.0";
	
	
	public static String getImplementation() {
		return implementation;
	}
	/**
	 * Return a string describing the version of this software.
	 * 
	 * @return A string representation of the software version.
	 */
	public static String getVersion() {
		return version;
	}
	
	/**
	 * Create and return a new <code>TorConfig</code> instance.
	 * 
	 * @param logManager This is a required dependency.  You must create a <code>LogManager</code>
	 *                   before calling this method to create a <code>TorConfig</code>
	 * @return A new <code>TorConfig</code> instance.
	 * @see TorConfig
	 */
	static public TorConfig createConfig() {
		return (TorConfig) Proxy.newProxyInstance(TorConfigProxy.class.getClassLoader(), new Class[] { TorConfig.class }, new TorConfigProxy());
	}

	static public TorInitializationTracker createInitalizationTracker() {
		return new TorInitializationTracker();
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
	static public Directory createDirectory(TorConfig config) {
		return new DirectoryImpl(config);
	}

	static public ConnectionCache createConnectionCache(TorInitializationTracker tracker) {
		return new ConnectionCacheImpl(tracker);
	}
	/**
	 * Create and return a new <code>CircuitManager</code> instance.
	 * 
	 * @return A new <code>CircuitManager</code> instance.
	 * @see CircuitManager
	 */
	static public CircuitManager createCircuitManager(TorConfig config, Directory directory, ConnectionCache connectionCache, TorInitializationTracker tracker) {
		return new CircuitManagerImpl(config, directory, connectionCache, tracker);
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
	static public SocksPortListener createSocksPortListener(CircuitManager circuitManager) {
		return new SocksPortListenerImpl(circuitManager);
	}

	/**
	 * Create and return a new <code>DirectoryDownloader</code> instance.
	 *
	 * @param logManager This is a required dependency.  You must create a <code>LogManager</code>
	 *                   before calling this method to create a <code>DirectoryDownloader</code>.

	 * @param directory This is a required dependency.  You must create a <code>Directory</code>
	 *                  before calling this method to create a <code>DirectoryDownloader</code>
	 *                  
	 * @param circuitManager This is a required dependency.  You must create a <code>CircuitManager</code>
	 *                       before calling this method to create a <code>DirectoryDownloader</code>.
	 *                       
	 * @return A new <code>DirectoryDownloader</code> instance.
	 * @see DirectoryDownloader
	 */
	static public DirectoryDownloader createDirectoryDownloader(Directory directory, CircuitManager circuitManager) {
		return new DirectoryDownloader(directory, circuitManager);
	}
}
