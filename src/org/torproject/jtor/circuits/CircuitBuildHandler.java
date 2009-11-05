package org.torproject.jtor.circuits;

public interface CircuitBuildHandler {
	void connectionCompleted(Connection connection);
	void connectionFailed(String reason);
	void nodeAdded(CircuitNode node);
	void circuitBuildCompleted(Circuit circuit);
	void circuitBuildFailed(String reason);
}
