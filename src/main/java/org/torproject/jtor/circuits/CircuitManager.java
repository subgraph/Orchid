package org.torproject.jtor.circuits;

import org.torproject.jtor.data.IPv4Address;


public interface CircuitManager {
	/**
	 * Create and return a new unconnected {@link Circuit} instance.
	 * 
	 * @return The new {@link Circuit} instance.
	 */
	Circuit createNewCircuit();

	/**
	 * Begin automatically building new circuits in the background.
	 */
	void startBuildingCircuits();

	/**
	 * Attempt to open an exit stream to the specified destination <code>hostname</code> and
	 * <code>port</code>.
	 * 
	 * @param hostname The name of the host to open an exit connection to.
	 * @param port The port to open an exit connection to.
	 * @return The status response result of attempting to open the exit connection.
	 */
	OpenStreamResponse openExitStreamTo(String hostname, int port) throws InterruptedException;

	/**
	 * Attempt to open an exit stream to the destination specified by <code>address</code> and
	 * <code>port</code>.
	 * 
	 * @param address The address to open an exit connection to.
	 * @param port The port to open an exit connection to.
	 * @return The status response result of attempting the open the exit connection.
	 */
	OpenStreamResponse openExitStreamTo(IPv4Address address, int port) throws InterruptedException;
}
