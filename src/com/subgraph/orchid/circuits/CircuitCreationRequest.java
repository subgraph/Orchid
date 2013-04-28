package com.subgraph.orchid.circuits;

import java.util.Collections;
import java.util.List;

import com.subgraph.orchid.Circuit;
import com.subgraph.orchid.CircuitBuildHandler;
import com.subgraph.orchid.CircuitNode;
import com.subgraph.orchid.Connection;
import com.subgraph.orchid.Router;
import com.subgraph.orchid.circuits.path.CircuitPathChooser;
import com.subgraph.orchid.circuits.path.PathSelectionFailedException;

public class CircuitCreationRequest implements CircuitBuildHandler {
	private final CircuitBase circuit;
	private final CircuitPathChooser pathChooser;
	private final CircuitBuildHandler buildHandler;
	
	private List<Router> path;
	
	CircuitCreationRequest(CircuitPathChooser pathChooser, CircuitBase circuit, CircuitBuildHandler buildHandler) {
		this.pathChooser = pathChooser;
		this.circuit = circuit;
		this.buildHandler = buildHandler;
		this.path = Collections.emptyList();
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
