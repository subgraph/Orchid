package org.torproject.jtor.circuits.impl;

import java.nio.ByteBuffer;

import org.torproject.jtor.TorException;
import org.torproject.jtor.circuits.CircuitNode;
import org.torproject.jtor.circuits.cells.Cell;
import org.torproject.jtor.circuits.cells.RelayCell;

public class RelayCellImpl extends CellImpl implements RelayCell {
	
	public static RelayCell createFromCell(CircuitNode node, Cell cell) {
		if(cell.getCommand() != Cell.RELAY)
			throw new TorException("Attempted to create RelayCell from Cell type: "+ cell.getCommand());
		return new RelayCellImpl(node, cell.getCellBytes());
	}
	
	private final int streamId;
	private final int relayCommand;
	private final CircuitNode circuitNode;
	
	/*
	 * The payload of each unencrypted RELAY cell consists of:
     *     Relay command           [1 byte]
     *     'Recognized'            [2 bytes]
     *     StreamID                [2 bytes]
     *     Digest                  [4 bytes]
     *     Length                  [2 bytes]
     *     Data                    [CELL_LEN-14 bytes]
     */
	 RelayCellImpl(CircuitNode node, int circuit, int stream, int relayCommand) {
		super(circuit, Cell.RELAY);
		this.circuitNode = node;
		this.relayCommand = relayCommand;
		this.streamId = stream;
		putByte(relayCommand);	// Command
		putShort(0);			// 'Recognized'
		putShort(stream);		// Stream
		putInt(0);				// Digest
		putShort(0);			// Length	
	}
	 
	private RelayCellImpl(CircuitNode node, byte[] rawCell) {
		super(rawCell);
		this.circuitNode = node;
		this.relayCommand = getByte();
		getShort();
		this.streamId = getShort();
		getInt();
		int payloadLength = getShort();
		cellBuffer.mark(); // End of header
		if(RelayCell.HEADER_SIZE + payloadLength > rawCell.length)
			throw new TorException("Header length field exceeds total size of cell");
		cellBuffer.limit(RelayCell.HEADER_SIZE + payloadLength);
	}
	
	public int getStreamId() {
		return streamId;
	}
	
	public int getRelayCommand() {
		return relayCommand;
	}
	
	public void setLength() {
		putShortAt(LENGTH_OFFSET, (short) (cellBytesConsumed() - HEADER_SIZE));
	}
	
	public void setDigest(byte[] digest) {
		for(int i = 0; i < 4; i++)
			putByteAt(DIGEST_OFFSET + i, digest[i]);
	}
	
	public ByteBuffer getPayloadBuffer() {
		final ByteBuffer dup = cellBuffer.duplicate();
		dup.reset();
		return dup.slice();
	}
	
	public CircuitNode getCircuitNode() {
		return circuitNode;
	}


}
