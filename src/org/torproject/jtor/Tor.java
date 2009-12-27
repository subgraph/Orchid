package org.torproject.jtor;



import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.torproject.jtor.circuits.Circuit;
import org.torproject.jtor.circuits.impl.CircuitManagerImpl;
import org.torproject.jtor.circuits.impl.ConnectionManagerImpl;
import org.torproject.jtor.circuits.impl.StreamManagerImpl;
import org.torproject.jtor.config.impl.TorConfigImpl;
import org.torproject.jtor.directory.Directory;
import org.torproject.jtor.directory.impl.DirectoryImpl;
import org.torproject.jtor.directory.impl.DocumentParserFactoryImpl;
import org.torproject.jtor.directory.impl.NetworkStatusManager;
import org.torproject.jtor.directory.parsing.DocumentParserFactory;
import org.torproject.jtor.logging.LogManager;
import org.torproject.jtor.logging.impl.LogManagerImpl;
import org.torproject.jtor.socks.SocksPortListener;
import org.torproject.jtor.socks.impl.SocksPortListenerImpl;

public class Tor {
	private final Directory directory;
	private final DocumentParserFactory parserFactory;
	private final ConnectionManagerImpl connectionManager;
	private final CircuitManagerImpl circuitManager;
	private final StreamManagerImpl streamManager;
	private final LogManager logManager;
	private final TorConfig config;
	private final NetworkStatusManager statusManager;
	private final SocksPortListener socksListener;
	
	public Tor() {
		Security.addProvider(new BouncyCastleProvider());
		this.logManager = new LogManagerImpl();
		this.config = new TorConfigImpl();
		this.directory = new DirectoryImpl(logManager, config);
		parserFactory = new DocumentParserFactoryImpl(logManager);
		connectionManager = new ConnectionManagerImpl(logManager);
		streamManager = new StreamManagerImpl();
		circuitManager = new CircuitManagerImpl(directory, connectionManager, streamManager, logManager);
		statusManager = new NetworkStatusManager(directory, logManager);
		socksListener = new SocksPortListenerImpl(logManager, streamManager);
	}
	
	
	public void start() {
		directory.loadFromStore();
		statusManager.startDownloadingDocuments();
		circuitManager.startBuildingCircuits();
		socksListener.addListeningPort(5090);
	}

	public Circuit createCircuit() {
		return circuitManager.newCircuit();
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
