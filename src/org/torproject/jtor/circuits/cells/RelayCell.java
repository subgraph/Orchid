package org.torproject.jtor.circuits.cells;

import java.nio.ByteBuffer;

import org.torproject.jtor.circuits.CircuitNode;


public interface RelayCell extends Cell {

	final static int LENGTH_OFFSET = 12;
	final static int RECOGNIZED_OFFSET = 4;
	final static int DIGEST_OFFSET = 8;
	final static int HEADER_SIZE = 14;

	final static int RELAY_BEGIN = 1;
	final static int RELAY_DATA = 2;
	final static int RELAY_END = 3;
	final static int RELAY_CONNECTED = 4;
	final static int RELAY_SENDME = 5;
	final static int RELAY_EXTEND = 6;
	final static int RELAY_EXTENDED = 7;
	final static int RELAY_TRUNCATE = 8;
	final static int RELAY_TRUNCATED = 9;
	final static int RELAY_DROP = 10;
	final static int RELAY_RESOLVE = 11;
	final static int RELAY_RESOLVED = 12;
	final static int RELAY_BEGIN_DIR = 13;

	final static int REASON_MISC = 1;
	final static int REASON_RESOLVEFAILED = 2;
	final static int REASON_CONNECTREFUSED = 3;
	final static int REASON_EXITPOLICY = 4;
	final static int REASON_DESTROY = 5;
	final static int REASON_DONE = 6;
	final static int REASON_TIMEOUT = 7;
	final static int REASON_HIBERNATING = 9;
	final static int REASON_INTERNAL = 10;
	final static int REASON_RESOURCELIMIT = 11;
	final static int REASON_CONNRESET = 12;
	final static int REASON_TORPROTOCOL = 13;
	final static int REASON_NOTDIRECTORY = 14;

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
