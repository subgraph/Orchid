package org.torproject.jtor.circuits;

import org.torproject.jtor.circuits.cells.RelayCell;
import org.torproject.jtor.directory.Router;

public interface Circuit {
	/**
	 * 
	 * @throws ConnectionConnectException Network connection to the first router in the chain failed.
	 */
	boolean openCircuit(CircuitBuildHandler callback);
	void extendCircuit(Router router);
	boolean isConnected();
	Connection getConnection();
	int getCircuitId();
	Stream openDirectoryStream();
	RelayCell createRelayCell(int relayCommand, int streamId, CircuitNode targetNode);
	RelayCell receiveRelayResponse(int expectedType);
	void sendRelayCell(RelayCell cell);
}
