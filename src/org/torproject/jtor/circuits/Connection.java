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
	 * @throws ConnectionConnectException If an error occured while attempting to establish the connection.
	 */
	void connect() throws ConnectionConnectException;

	/**
	 * Send a protocol {@link Cell} on this connection.
	 * 
	 * @param cell The {@link Cell} to transfer.
	 * @throws ConnectionClosedException If the cell could not be send because the connection is not connected
	 *                                   or if an error occured while sending the cell data.
	 */
	void sendCell(Cell cell);
}
