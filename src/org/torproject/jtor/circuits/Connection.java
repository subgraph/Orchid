package org.torproject.jtor.circuits;

import org.torproject.jtor.circuits.cells.Cell;
import org.torproject.jtor.directory.Router;

public interface Connection {
	/**
	 * Return the {@link Router} associated with this connection.
	 * 
	 * @return The entry router this connection represents.
	 */
	Router getRouter();

	/**
	 * Return <code>true</code> if this connection is currently connected.  Otherwise, <code>false</code>.
	 * 
	 * @return <code>true</code> if this connection is connected or <code>false</code> otherwise.
	 */
	boolean isConnected();

	/**
	 * If the network connection is not currently connected, attempt to open it.  If already connected
	 * return immediately.
	 * 
	 * @throws ConnectionFailedException If an error occured while attempting to establish the connection.
	 * @throws ConnectionTimeoutException If connection failed due to timer expiry.
	 * @throws ConnectionHandshakeException If connection failed during handshake stage.
	 */
	//void connect() throws ConnectionFailedException, ConnectionTimeoutException, ConnectionHandshakeException;

	/**
	 * Send a protocol {@link Cell} on this connection.
	 * 
	 * @param cell The {@link Cell} to transfer.
	 * @throws ConnectionIOException If the cell could not be send because the connection is not connected
	 *                                   or if an error occured while sending the cell data.
	 */
	void sendCell(Cell cell) throws ConnectionIOException;
	
	void removeCircuit(Circuit circuit);
	int allocateCircuitId(Circuit circuit);

}
