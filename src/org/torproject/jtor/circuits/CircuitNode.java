package org.torproject.jtor.circuits;

import org.torproject.jtor.circuits.cells.RelayCell;
import org.torproject.jtor.directory.Router;

public interface CircuitNode {
	Router getRouter();
	void updateForwardDigest(RelayCell cell);
	byte[] getForwardDigestBytes();
	void encryptForwardCell(RelayCell cell);
	CircuitNode getPreviousNode();

}
