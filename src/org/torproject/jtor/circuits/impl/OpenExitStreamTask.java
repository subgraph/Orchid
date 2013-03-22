package org.torproject.jtor.circuits.impl;

import java.util.logging.Logger;

import org.torproject.jtor.circuits.Circuit;
import org.torproject.jtor.circuits.OpenStreamResponse;

public class OpenExitStreamTask implements Runnable {
	private final static Logger logger = Logger.getLogger(OpenExitStreamTask.class.getName());
	private final Circuit circuit;
	private final StreamExitRequest exitRequest;

	OpenExitStreamTask(Circuit circuit, StreamExitRequest exitRequest) {
		this.circuit = circuit;
		this.exitRequest = exitRequest;
	}

	public void run() {
		logger.fine("Attempting to open stream to "+ exitRequest);
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
