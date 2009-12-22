package org.torproject.jtor;


import java.security.Security;
import java.util.List;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.torproject.jtor.circuits.Circuit;
import org.torproject.jtor.circuits.impl.CircuitManagerImpl;
import org.torproject.jtor.circuits.impl.ConnectionManagerImpl;
import org.torproject.jtor.circuits.impl.StreamManagerImpl;
import org.torproject.jtor.config.impl.TorConfigImpl;
import org.torproject.jtor.directory.Directory;
import org.torproject.jtor.directory.Router;
import org.torproject.jtor.directory.impl.DirectoryImpl;
import org.torproject.jtor.directory.impl.DocumentParserFactoryImpl;
import org.torproject.jtor.directory.impl.NetworkStatusManager;
import org.torproject.jtor.directory.parsing.DocumentParserFactory;
import org.torproject.jtor.socks.SocksPortListener;
import org.torproject.jtor.socks.impl.SocksPortListenerImpl;

public class Tor {
	private final Directory directory;
	private final DocumentParserFactory parserFactory;
	private final ConnectionManagerImpl connectionManager;
	private final CircuitManagerImpl circuitManager;
	private final StreamManagerImpl streamManager;
	private final Logger logger;
	private final TorConfig config;
	private final NetworkStatusManager statusManager;
	private final SocksPortListener socksListener;
	
	public Tor() {
		this(new ConsoleLogger());
	}
	
	public Tor(Logger logger) {
		Security.addProvider(new BouncyCastleProvider());
		this.logger = logger;
		this.config = new TorConfigImpl();
		this.directory = new DirectoryImpl(logger, config);
		parserFactory = new DocumentParserFactoryImpl(logger);
		connectionManager = new ConnectionManagerImpl(logger);
		streamManager = new StreamManagerImpl();
		circuitManager = new CircuitManagerImpl(directory, connectionManager, streamManager, logger);
		statusManager = new NetworkStatusManager(directory, logger);
		socksListener = new SocksPortListenerImpl(logger, streamManager);
	}
	
	
	public void start() {
		directory.loadFromStore();
		statusManager.startDownloadingDocuments();
		circuitManager.startBuildingCircuits();
		socksListener.addListeningPort(5090);
	}
	
	public Circuit createCircuitFromNicknames(List<String> nicknamePath) {
		final List<Router> path = directory.getRouterListByNames(nicknamePath);
		return createCircuit(path);
	}
	
	public Circuit createCircuit(List<Router> path) {
		return circuitManager.createCircuitFromPath(path);
	}
	
	public Logger getLogger() {
		return logger;
	}
	
	public Directory getDirectory() {
		return directory;
	}
	
	public TorConfig getConfig() {
		return config;
	}
	
	public DocumentParserFactory getDocumentParserFactory() {
		return parserFactory;
	}

	public ConnectionManagerImpl getConnectionManager() {
		return connectionManager;
	}
	
}
