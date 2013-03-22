package org.torproject.jtor.circuits.impl;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.torproject.jtor.circuits.Circuit;
import org.torproject.jtor.circuits.CircuitBuildHandler;
import org.torproject.jtor.directory.Router;

public class OpenCircuitTask implements Runnable {
	private final static Logger logger = Logger.getLogger(OpenCircuitTask.class.getName());
	private final Circuit circuit;
	private final List<Router> circuitPath;
	private final CircuitBuildHandler buildHandler;
	
	OpenCircuitTask(Circuit circuit, List<Router> circuitPath, CircuitBuildHandler handler) {
		this.circuit = circuit;
		this.circuitPath = circuitPath;
		this.buildHandler = handler;
	}
	
	public void run() {
		if(logger.isLoggable(Level.FINE)) {
			logger.fine("Opening a new circuit to "+ pathToString());
		}
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
