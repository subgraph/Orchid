package org.torproject.jtor.circuits;


public interface CircuitManager {
	Circuit newCircuit();
	void startBuildingCircuits();
	Stream getDirectoryStream();
}
