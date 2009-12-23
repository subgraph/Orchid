package org.torproject.jtor.circuits;

import java.util.List;

import org.torproject.jtor.circuits.cells.RelayCell;
import org.torproject.jtor.data.IPv4Address;
import org.torproject.jtor.directory.Router;

/**
 * A Circuit represents a logical path through multiple ORs.  Circuits are described in
 * section 5 of tor-spec.txt.
 *
 */
public interface Circuit {
	boolean openCircuit(List<Router> circuitPath, CircuitBuildHandler callback);
	void extendCircuit(Router router);
	boolean isConnected();
	Connection getConnection();
	int getCircuitId();
	Stream openDirectoryStream();
	Stream openExitStream(IPv4Address address, int port);
	Stream openExitStream(String hostname, int port);
	RelayCell createRelayCell(int relayCommand, int streamId, CircuitNode targetNode);
	RelayCell receiveRelayCell();
	void sendRelayCell(RelayCell cell);
	CircuitNode getFinalCircuitNode();
}
