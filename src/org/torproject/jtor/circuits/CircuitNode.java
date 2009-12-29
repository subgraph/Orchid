package org.torproject.jtor.circuits;

import org.torproject.jtor.circuits.cells.RelayCell;
import org.torproject.jtor.directory.Router;

/**
 * Represents the state of a single onion router hop in a connected or connecting {@link Circuit}
 */
public interface CircuitNode {
	/**
	 * Return the {@link Router} associated with this node.
	 *
	 * @return The {@link Router} for this hop of the circuit chain.
	 */
	Router getRouter();

	/**
	 * Update the 'forward' cryptographic digest state for this
	 * node with the contents of <code>cell</code>
	 * 
	 * @param cell The {@link RelayCell} to add to the digest.
	 */
	void updateForwardDigest(RelayCell cell);

	/**
	 * Return the current 'forward' running digest value for this 
	 * node as an array of <code>TOR_DIGEST_SIZE</code> bytes.
	 * 
	 * @return The current 'forward' running digest value for this node.
	 */
	byte[] getForwardDigestBytes();

	/**
	 * Encrypt a {@link RelayCell} for this node with the current
	 * 'forward' cipher state.
	 * 
	 * @param cell The {@link RelayCell} to encrypt.
	 */
	void encryptForwardCell(RelayCell cell);

	/**
	 * Return the {@link CircuitNode} which immediately preceeds this
	 * one in the circuit node chain or <code>null</code> if this is
	 * the first hop.
	 * 
	 * @return The previous {@link CircuitNode} in the chain or <code>
	 *         null</code> if this is the first node.
	 */
	CircuitNode getPreviousNode();
}
