package org.torproject.jtor.circuits;

import org.torproject.jtor.directory.RouterDescriptor;

public interface Circuit {
	/**
	 * 
	 * @throws ConnectionConnectException Network connection to the first router in the chain failed.
	 */
	void openCircuit();
	void extendCircuit(RouterDescriptor router);

	Connection getConnection();
	int getCircuitId();
}
