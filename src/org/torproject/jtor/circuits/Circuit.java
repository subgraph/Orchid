package org.torproject.jtor.circuits;

import java.util.List;

import org.torproject.jtor.circuits.cells.RelayCell;
import org.torproject.jtor.data.IPv4Address;
import org.torproject.jtor.data.exitpolicy.ExitTarget;
import org.torproject.jtor.directory.Router;

/**
 * A Circuit represents a logical path through multiple ORs.  Circuits are described in
 * section 5 of tor-spec.txt.
 *
 */
public interface Circuit {
	/**
	 * Open this circuit by connecting the specified <code>circuitPath</code> and notifying
	 * progress, success, and failure through <code>callback</code>.
	 * 
	 * @param circuitPath The path of onion routers to use for this circuit.
	 * @param callback A callback structure for notifying the caller about the status of the
	 *                 opening circuit.
	 *                
	 * @see CircuitBuildHandler
	 */
	void openCircuit(List<Router> circuitPath, CircuitBuildHandler callback);
	
	/**
	 * Extend an already connected circuit to add an additional <code>Router</code> to the path.
	 * 
	 * @param router The router to add to the circuit.
	 */
	void extendCircuit(Router router);
	
	/**
	 * Return <code>true</code> if the circuit is presently in the connected state or
	 * <code>false</code> otherwise.
	 * 
	 * @return Returns <code>true</code> if the circuit is presently connected, or 
	 *                 <code>false</code> otherwise.
	 */
	boolean isConnected();
	
	/**
	 * Returns the entry router <code>Connection</code> object of this Circuit.  Throws
	 * a TorException if the circuit is not currently open.
	 *  
	 * @return The Connection object for the network connection to the entry router of this 
	 *         circuit.
	 * @throws TorException If this circuit is not currently connected.
	 */
	Connection getConnection();
	
	/**
	 * Returns the curcuit id value for this circuit.
	 * 
	 * @return The circuit id value for this circuit.
	 */
	int getCircuitId();
	
	/**
	 * Open an anonymous connection to the directory service running on the
	 * final node in this circuit.
	 * 
	 * @return The status response returned by trying to open the stream.
	 */
	OpenStreamResponse openDirectoryStream();
	
	/**
	 * Open an exit stream from the final node in this circuit to the 
	 * specified target address and port.
	 * 
	 * @param address The network address of the exit target.
	 * @param port The port of the exit target.
	 * @return The status response returned by trying to open the stream.
	 */
	OpenStreamResponse openExitStream(IPv4Address address, int port);
	
	/**
	 * Open an exit stream from the final node in this circuit to the
	 * specified target hostname and port.
	 * 
	 * @param hostname The network hostname of the exit target.
	 * @param port The port of the exit target.
	 * @return The status response returned by trying to open the stream.
	 */
	OpenStreamResponse openExitStream(String hostname, int port);
	
	/**
	 * Create a new relay cell which is configured for delivery to the specified
	 * circuit <code>targetNode</code> with command value <code>relayCommand</code>
	 * and a stream id value of <code>streamId</code>.  The returned <code>RelayCell</code>
	 * can then be used to populate the payload of the cell before delivering it.
	 * 
	 * @param relayCommand The command value to send in the relay cell header.
	 * @param streamId The stream id value to send in the relay cell header.
	 * @param targetNode The target circuit node to encrypt this cell for.
	 * @return A newly created relay cell object.
	 */
	RelayCell createRelayCell(int relayCommand, int streamId, CircuitNode targetNode);
	
	/**
	 * Returns the next relay response cell received on this circuit.  If no response is
	 * received within <code>CIRCUIT_RELAY_RESPONSE_TIMEOUT</code> milliseconds, <code>null</code>
	 * is returned.
	 * 
	 * @return The next relay response cell received on this circuit or <code>null</code> if
	 *         a timeout is reached before the next relay cell arrives.
	 */
	RelayCell receiveRelayCell();
	
	/**
	 * Encrypt and deliver the relay cell <code>cell</code>.
	 * 
	 * @param cell The relay cell to deliver over this circuit.
	 */
	void sendRelayCell(RelayCell cell);
	
	/**
	 * Return the last node or 'hop' in this circuit.
	 * 
	 * @return The final 'hop' or node of this circuit.
	 */
	CircuitNode getFinalCircuitNode();

	/**
	 * Return true if the final node of this circuit is believed to be able to connect to
	 * the specified <code>ExitTarget</code>.  Returns false if the target destination is
	 * not permitted by the exit policy of the final node in this circuit or if the target
	 * has been previously recorded to have failed through this circuit.
	 * 
	 * @param target The exit destination.
	 * @return Return true if is likely that the final node of this circuit can connect to the specified exit target.
	 */
	boolean canHandleExitTo(ExitTarget target);
	
	/**
	 * Records the specified <code>ExitTarget</code> as a failed connection so that {@link #canHandleExitTo(ExitTarget)} will
	 * no longer return true for this exit destination.
	 * 
	 * @param target The <code>ExitTarget</code> to which a connection has failed through this circuit.
	 */
	public void recordFailedExitTarget(ExitTarget target);
}
