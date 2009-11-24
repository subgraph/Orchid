package org.torproject.jtor;


import java.security.Security;
import java.util.List;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.torproject.jtor.circuits.Circuit;
import org.torproject.jtor.circuits.impl.CircuitManagerImpl;
import org.torproject.jtor.circuits.impl.ConnectionManagerImpl;
import org.torproject.jtor.config.impl.TorConfigImpl;
import org.torproject.jtor.directory.Directory;
import org.torproject.jtor.directory.Router;
import org.torproject.jtor.directory.impl.DirectoryImpl;
import org.torproject.jtor.directory.impl.DocumentParserFactoryImpl;
import org.torproject.jtor.directory.impl.NetworkStatusManager;
import org.torproject.jtor.directory.parsing.DocumentParserFactory;

public class Tor {
	private final Directory directory;
	private final DocumentParserFactory parserFactory;
	private final ConnectionManagerImpl connectionManager;
	private final CircuitManagerImpl circuitManager;
	private final Logger logger;
	private final TorConfig config;
	private final NetworkStatusManager statusManager;
	
	public Tor() {
		this(new ConsoleLogger());
	}
	
	public Tor(Logger logger) {
		Security.addProvider(new BouncyCastleProvider());
		this.logger = logger;
		this.config = new TorConfigImpl();
		this.directory = new DirectoryImpl(logger, config);
		parserFactory = new DocumentParserFactoryImpl(logger);
		connectionManager = new ConnectionManagerImpl();
		circuitManager = new CircuitManagerImpl(directory, connectionManager);
		statusManager = new NetworkStatusManager(directory, logger);
	}
	
	
	public void start() {
		directory.loadFromStore();
		statusManager.startDownloadingDocuments();
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
