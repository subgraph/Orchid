package com.subgraph.orchid.directory.downloader;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import com.subgraph.orchid.CircuitManager;
import com.subgraph.orchid.Directory;
import com.subgraph.orchid.Router;
import com.subgraph.orchid.RouterDescriptor;
import com.subgraph.orchid.TorConfig;
import com.subgraph.orchid.directory.DocumentParserFactoryImpl;
import com.subgraph.orchid.directory.parsing.DocumentParserFactory;

public class DirectoryDownloader {
	private final static Logger logger = Logger.getLogger(DirectoryDownloader.class.getName());
	private final static DocumentParserFactory parserFactory = new DocumentParserFactoryImpl();
	
	private final TorConfig config;
	private final ExecutorService executor;
	private CircuitManager circuitManager;
	private boolean isStarted;
	private Thread downloadTaskThread;
	

	public DirectoryDownloader(TorConfig config) {
		this.config = config;
		this.executor = Executors.newCachedThreadPool();
	}

	public void setCircuitManager(CircuitManager circuitManager) {
		this.circuitManager = circuitManager;
	}

	public synchronized void start(Directory directory) {
		if(isStarted) {
			logger.warning("Directory downloader already running");
			return;
		}
		if(circuitManager == null) {
			throw new IllegalStateException("Must set CircuitManager instance with setCircuitManager() before starting.");
		}
	
		final DirectoryDownloadTask task = new DirectoryDownloadTask(config, directory, circuitManager);
		downloadTaskThread = new Thread(task);
		downloadTaskThread.start();
		isStarted = true;
	}

	public RouterDescriptor downloadBridgeDescriptor(Router bridge) {
		if(circuitManager == null) {
			throw new IllegalStateException("Must set CircuitManager instance with setCircuitManager()");
		}
		BridgeDescriptorDownloadTask task = new BridgeDescriptorDownloadTask(parserFactory, circuitManager, bridge);
		Future<RouterDescriptor> future = executor.submit(task);
		try {
			return future.get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (ExecutionException e) {
			logger.warning("Failed to download bridge descriptor for "+ bridge +" : "+ e.getCause().getMessage());
		}
		return null;
	}


}
