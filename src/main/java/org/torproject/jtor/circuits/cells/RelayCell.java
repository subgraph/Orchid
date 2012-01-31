package org.torproject.jtor.circuits.cells;

import java.nio.ByteBuffer;

import org.torproject.jtor.circuits.CircuitNode;


public interface RelayCell extends Cell {

	static final int LENGTH_OFFSET = 12;
	static final int RECOGNIZED_OFFSET = 4;
	static final int DIGEST_OFFSET = 8;
	static final int HEADER_SIZE = 14;

	static final int RELAY_BEGIN = 1;
	static final int RELAY_DATA = 2;
	static final int RELAY_END = 3;
	static final int RELAY_CONNECTED = 4;
	static final int RELAY_SENDME = 5;
	static final int RELAY_EXTEND = 6;
	static final int RELAY_EXTENDED = 7;
	static final int RELAY_TRUNCATE = 8;
	static final int RELAY_TRUNCATED = 9;
	static final int RELAY_DROP = 10;
	static final int RELAY_RESOLVE = 11;
	static final int RELAY_RESOLVED = 12;
	static final int RELAY_BEGIN_DIR = 13;

	static final int REASON_MISC = 1;
	static final int REASON_RESOLVEFAILED = 2;
	static final int REASON_CONNECTREFUSED = 3;
	static final int REASON_EXITPOLICY = 4;
	static final int REASON_DESTROY = 5;
	static final int REASON_DONE = 6;
	static final int REASON_TIMEOUT = 7;
	static final int REASON_HIBERNATING = 9;
	static final int REASON_INTERNAL = 10;
	static final int REASON_RESOURCELIMIT = 11;
	static final int REASON_CONNRESET = 12;
	static final int REASON_TORPROTOCOL = 13;
	static final int REASON_NOTDIRECTORY = 14;

	int getStreamId();
	int getRelayCommand();
	/**
	 * Return the circuit node this cell was received from for outgoing cells or the destination circuit node
	 * for outgoing cells.
	 */
	CircuitNode getCircuitNode();
	ByteBuffer getPayloadBuffer();
	void setLength();
	void setDigest(byte[] digest);
}
