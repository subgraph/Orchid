package org.torproject.jtor.circuits.impl;

import org.torproject.jtor.circuits.Circuit;
import org.torproject.jtor.circuits.OpenStreamResponse;
import org.torproject.jtor.logging.Logger;

public class OpenExitStreamTask implements Runnable {

	private final Circuit circuit;
	private final StreamExitRequest exitRequest;
	private final Logger logger;

	OpenExitStreamTask(Circuit circuit, StreamExitRequest exitRequest, Logger logger) {
		this.circuit = circuit;
		this.exitRequest = exitRequest;
		this.logger = logger;
	}

	public void run() {
		logger.debug("Attempting to open stream to "+ exitRequest);
		final OpenStreamResponse openStreamResponse = tryOpenExitStream();
		switch(openStreamResponse.getStatus()) {
		case STATUS_STREAM_OPENED:
			break;
		case STATUS_STREAM_ERROR:
		case STATUS_STREAM_TIMEOUT:
			circuit.recordFailedExitTarget(exitRequest);
			break;
		}
		exitRequest.setCompleted(openStreamResponse);
	}

	private OpenStreamResponse tryOpenExitStream() {
		if(exitRequest.isAddressTarget())
			return circuit.openExitStream(exitRequest.getAddress(), exitRequest.getPort());
		else
			return circuit.openExitStream(exitRequest.getHostname(), exitRequest.getPort());
	}

}
