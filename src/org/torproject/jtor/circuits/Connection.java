package org.torproject.jtor.circuits;

import org.torproject.jtor.circuits.cells.Cell;
import org.torproject.jtor.directory.Router;

public interface Connection {
	Router getRouter();
	boolean isConnected();
	void connect() throws ConnectionConnectException;
	/**
	 * 
	 * @param cell
	 * @throws ConnectionClosedException
	 */
	void sendCell(Cell cell);
	
	
}
