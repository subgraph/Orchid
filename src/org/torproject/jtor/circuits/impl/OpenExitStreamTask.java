package org.torproject.jtor.circuits.impl;

import org.torproject.jtor.circuits.Circuit;
import org.torproject.jtor.circuits.Stream;
import org.torproject.jtor.logging.Logger;

public class OpenExitStreamTask implements Runnable {

	private final Circuit circuit;
	private final StreamExitRequest exitRequest;
	private final StreamManagerImpl streamManager;
	private final Logger logger;
	
	OpenExitStreamTask(Circuit circuit, StreamExitRequest exitRequest, StreamManagerImpl streamManager, Logger logger) {
		this.circuit = circuit;
		this.exitRequest = exitRequest;
		this.streamManager = streamManager;
		this.logger = logger;
	}
	
	public void run() {
		logger.debug("Attempting to open stream to "+ exitRequest);
		final Stream stream = tryOpenExitStream();
		if(stream == null) {
			exitRequest.unreserveRequest();
			return;
		}
		exitRequest.setAllocatedStream(stream);
		streamManager.streamIsConnected(exitRequest);		
	}
	
	private Stream tryOpenExitStream() {
		if(exitRequest.isAddressRequest())
			return circuit.openExitStream(exitRequest.getAddress(), exitRequest.getPort());
		else
			return circuit.openExitStream(exitRequest.getHostname(), exitRequest.getPort());
	}

}
