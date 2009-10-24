package org.torproject.jtor.circuits.cells;


public class RelayCell extends Cell {
	
	public final static int RELAY_LENGTH_OFFSET = 12;
	public final static int RELAY_RECOGNIZED_OFFSET = 4;
	public final static int RELAY_DIGEST_OFFSET = 8;
	private final static int HEADER_SIZE = 14;
	
	public final static int RELAY_BEGIN = 1;
	public final static int RELAY_DATA = 2;
	public final static int RELAY_END = 3;
	public final static int RELAY_CONNECTED = 4;
	public final static int RELAY_SENDME = 5;
	public final static int RELAY_EXTEND = 6;
	public final static int RELAY_EXTENDED = 7;
	public final static int RELAY_TRUNCATE = 8;
	public final static int RELAY_TRUNCATED = 9;
	public final static int RELAY_DROP = 10;
	public final static int RELAY_RESOLVE = 11;
	public final static int RELAY_RESOLVED = 12;
	public final static int RELAY_BEGIN_DIR = 13;
	/*
	 * The payload of each unencrypted RELAY cell consists of:
     *     Relay command           [1 byte]
     *     'Recognized'            [2 bytes]
     *     StreamID                [2 bytes]
     *     Digest                  [4 bytes]
     *     Length                  [2 bytes]
     *     Data                    [CELL_LEN-14 bytes]
     */

	public RelayCell(int circuit, int stream, int relayCommand) {
		super(circuit, Cell.RELAY);
			
		putByte(relayCommand);	// Command
		putShort(0);			// 'Recognized'
		putShort(stream);		// Stream
		putInt(0);				// Digest
		putShort(0);			// Length
		
	}
	
	
	public void setLength() {
		putShortAt(RELAY_LENGTH_OFFSET, (short) (cellBytesConsumed() - HEADER_SIZE));
	}
	
	public void setDigest(byte[] digest) {
		for(int i = 0; i < 4; i++)
			putByteAt(RELAY_DIGEST_OFFSET + i, digest[i]);
	}

}
