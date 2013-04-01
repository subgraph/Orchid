package org.torproject.jtor.circuits.impl;

import java.util.List;

import org.torproject.jtor.circuits.Circuit;
import org.torproject.jtor.circuits.CircuitBuildHandler;
import org.torproject.jtor.circuits.CircuitNode;
import org.torproject.jtor.circuits.Connection;
import org.torproject.jtor.circuits.path.CircuitPathChooser;
import org.torproject.jtor.circuits.path.PathSelectionFailedException;
import org.torproject.jtor.directory.Router;

public class CircuitCreationRequest implements CircuitBuildHandler {
	private final CircuitBase circuit;
	private final CircuitPathChooser pathChooser;
	private final CircuitBuildHandler buildHandler;
	
	private List<Router> path;
	
	CircuitCreationRequest(CircuitPathChooser pathChooser, CircuitBase circuit, CircuitBuildHandler buildHandler) {
		this.pathChooser = pathChooser;
		this.circuit = circuit;
		this.buildHandler = buildHandler;
	}
	
	void choosePath() throws InterruptedException, PathSelectionFailedException {
		path = circuit.choosePath(pathChooser);
	}

	CircuitBase getCircuit() {
		return circuit;
	}

	List<Router> getPath() {
		return path;
	}
	
	int getPathLength() {
		return path.size();
	}
	
	Router getPathElement(int idx) {
		return path.get(idx);
	}
	
	CircuitBuildHandler getBuildHandler() {
		return buildHandler;
	}
	
	boolean isDirectoryCircuit() {
		return circuit.isDirectoryCircuit();
	}

	public void connectionCompleted(Connection connection) {
		if(buildHandler != null) {
			buildHandler.connectionCompleted(connection);
		}
	}

	public void connectionFailed(String reason) {
		if(buildHandler != null) {
			buildHandler.connectionFailed(reason);
		}
	}

	public void nodeAdded(CircuitNode node) {
		if(buildHandler != null) {
			buildHandler.nodeAdded(node);
		}
	}

	public void circuitBuildCompleted(Circuit circuit) {
		if(buildHandler != null) {
			buildHandler.circuitBuildCompleted(circuit);
		}
	}

	public void circuitBuildFailed(String reason) {
		if(buildHandler != null) {
			buildHandler.circuitBuildFailed(reason);
		}
	}
}
