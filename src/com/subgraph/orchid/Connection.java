package com.subgraph.orchid;

import com.subgraph.orchid.dashboard.DashboardRenderable;


public interface Connection extends DashboardRenderable {
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
	boolean isClosed();
	/**
	 * Send a protocol {@link Cell} on this connection.
	 * 
	 * @param cell The {@link Cell} to transfer.
	 * @throws ConnectionIOException If the cell could not be send because the connection is not connected
	 *                                   or if an error occured while sending the cell data.
	 */
	void sendCell(Cell cell) throws ConnectionIOException;
	
	void removeCircuit(Circuit circuit);
	
	int bindCircuit(Circuit circuit);
}
