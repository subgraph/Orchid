package org.torproject.jtor.circuits;

import org.torproject.jtor.circuits.cells.RelayCell;
import org.torproject.jtor.directory.RouterDescriptor;

public interface CircuitNode {
	RouterDescriptor getRouter();
	void updateForwardDigest(RelayCell cell);
	byte[] getForwardDigestBytes();
	void encryptForwardCell(RelayCell cell);
	CircuitNode getPreviousNode();

}
