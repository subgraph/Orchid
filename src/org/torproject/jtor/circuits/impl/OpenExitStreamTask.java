package org.torproject.jtor.circuits.impl;

import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import org.torproject.jtor.circuits.Circuit;
import org.torproject.jtor.circuits.Stream;
import org.torproject.jtor.circuits.StreamConnectFailedException;

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
		try {
			exitRequest.setCompletedSuccessfully(tryOpenExitStream());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			exitRequest.setInterrupted();
		} catch (TimeoutException e) {
			circuit.markForClose();
			exitRequest.setCompletedTimeout();
		} catch (StreamConnectFailedException e) {
			if(!e.isReasonRetryable()) {
				exitRequest.setExitFailed();
				circuit.recordFailedExitTarget(exitRequest);
			} else {
				circuit.markForClose();
				exitRequest.setStreamOpenFailure(e.getReason());
			}
			
		}
	}

	private Stream tryOpenExitStream() throws InterruptedException, TimeoutException, StreamConnectFailedException {
		if(exitRequest.isAddressTarget()) {
			return circuit.openExitStream(exitRequest.getAddress(), exitRequest.getPort(), exitRequest.getStreamTimeout());
		} else {
			return circuit.openExitStream(exitRequest.getHostname(), exitRequest.getPort(), exitRequest.getStreamTimeout());
		}
	}

}
