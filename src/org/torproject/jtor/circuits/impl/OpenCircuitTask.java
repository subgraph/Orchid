package org.torproject.jtor.circuits.impl;

import java.util.List;

import org.torproject.jtor.circuits.Circuit;
import org.torproject.jtor.circuits.CircuitBuildHandler;
import org.torproject.jtor.directory.Router;
import org.torproject.jtor.logging.Logger;

public class OpenCircuitTask implements Runnable {

	private final Circuit circuit;
	private final List<Router> circuitPath;
	private final CircuitBuildHandler buildHandler;
	private final Logger logger;
	
	OpenCircuitTask(Circuit circuit, List<Router> circuitPath, CircuitBuildHandler handler, Logger logger) {
		this.circuit = circuit;
		this.circuitPath = circuitPath;
		this.buildHandler = handler;
		this.logger = logger;
	}
	
	public void run() {
		logger.debug("Opening a new circuit to "+ pathToString());
		circuit.openCircuit(circuitPath, buildHandler);		
	}
	
	private String pathToString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("[");
		for(Router r: circuitPath) {
			if(sb.length() > 1)
				sb.append(",");
			sb.append(r.getNickname());
		}
		sb.append("]");
		return sb.toString();
	}

}
