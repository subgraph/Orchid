package org.torproject.jtor.circuits.impl;

import java.util.List;

import org.torproject.jtor.circuits.Circuit;
import org.torproject.jtor.circuits.CircuitBuildHandler;
import org.torproject.jtor.circuits.CircuitNode;
import org.torproject.jtor.circuits.Connection;
import org.torproject.jtor.circuits.path.CircuitPathChooser;
import org.torproject.jtor.data.exitpolicy.ExitTarget;
import org.torproject.jtor.directory.Router;

public class CircuitCreationRequest implements CircuitBuildHandler {
	private final CircuitImpl circuit;
	private final CircuitPathChooser pathChooser;
	private final List<ExitTarget> targets;
	private final CircuitBuildHandler buildHandler;
	private final boolean isDirectoryCircuit;
	
	private List<Router> path;
	
	CircuitCreationRequest(CircuitPathChooser pathChooser, CircuitImpl circuit, List<ExitTarget> targets, CircuitBuildHandler buildHandler, boolean isDirectoryCircuit) {
		this.pathChooser = pathChooser;
		this.circuit = circuit;
		this.targets = targets;
		this.buildHandler = buildHandler;
		this.isDirectoryCircuit = isDirectoryCircuit;
	}
	
	void choosePath() throws InterruptedException {
		if(isDirectoryCircuit) {
			path = pathChooser.chooseDirectoryPath();
		} else {
			path = pathChooser.choosePathForTargets(targets);
		}
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
