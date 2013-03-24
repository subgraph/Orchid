package org.torproject.jtor.circuits.impl;

import java.util.List;

import org.torproject.jtor.circuits.Circuit;
import org.torproject.jtor.circuits.CircuitBuildHandler;
import org.torproject.jtor.circuits.CircuitNode;
import org.torproject.jtor.circuits.Connection;
import org.torproject.jtor.directory.Router;

public class CircuitCreationRequest implements CircuitBuildHandler {
	private final CircuitImpl circuit;
	private final List<Router> path;
	private final CircuitBuildHandler buildHandler;
	private final boolean isDirectoryCircuit;
	
	CircuitCreationRequest(CircuitImpl circuit, List<Router> path, CircuitBuildHandler buildHandler, boolean isDirectoryCircuit) {
		if(path.isEmpty()) {
			throw new IllegalArgumentException("Path must contain at least one router to create a circuit.");
		}
		this.circuit = circuit;
		this.path = path;
		this.buildHandler = buildHandler;
		this.isDirectoryCircuit = isDirectoryCircuit;
	}
	
	CircuitImpl getCircuit() {
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
		return isDirectoryCircuit;
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
