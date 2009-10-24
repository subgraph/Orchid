package org.torproject.jtor.circuits.cells;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class Cell {
	public final static int PADDING = 0;
	public final static int CREATE = 1;
	public final static int CREATED = 2;
	public final static int RELAY = 3;
	public final static int DESTROY = 4;
	public final static int CREATE_FAST = 5;
	public final static int CREATED_FAST = 6;
	public final static int VERSIONS = 7;
	public final static int NETINFO = 8;
	public final static int RELAY_EARLY = 9;
	
	public static Cell createCell(int circuitId, int command) {
		return new Cell(circuitId, command);
	}
	
	public static Cell createVarCell(int circuitId, int command, int payloadLength) {
		return new Cell(circuitId, command, payloadLength);
	}
	
	public static Cell readFromInputStream(InputStream input) throws IOException {
		final ByteBuffer header = readHeaderFromInputStream(input);
		final int circuitId = header.getShort() & 0xFFFF;
		final int command = header.get() & 0xFF;
		if(command == VERSIONS)
			return readVarCell(circuitId, command, input);
		final Cell cell = new Cell(circuitId, command);
		readAll(input, cell.getCellBytes(), CELL_HEADER_LEN, CELL_PAYLOAD_LEN);
		return cell;
	}
	
	private static ByteBuffer readHeaderFromInputStream(InputStream input) throws IOException {
		final byte[] cellHeader = new byte[CELL_HEADER_LEN];
		readAll(input, cellHeader);
		return ByteBuffer.wrap(cellHeader);
	}
	
	private static Cell readVarCell(int circuitId, int command, InputStream input) throws IOException {
		final byte[] lengthField = new byte[2];
		readAll(input, lengthField);
		final int length = ((lengthField[0] & 0xFF) << 8) | (lengthField[1] & 0xFF);
		Cell cell = new Cell(circuitId, command, length);
		readAll(input, cell.getCellBytes(), CELL_VAR_HEADER_LEN, length);
		return cell;
	}
	
	private static void readAll(InputStream input, byte[] buffer) throws IOException {
		readAll(input, buffer, 0, buffer.length);
	}
	
	private static void readAll(InputStream input, byte[] buffer, int offset, int length) throws IOException {
		int bytesRead = 0;
		while(bytesRead < length) {
			final int n = input.read(buffer, offset + bytesRead, length - bytesRead);
			if(n == -1)
				throw new IOException("EOF reading var cell from stream");
			bytesRead += n;
		}
	}
	
	public final static int CELL_LEN = 512;
	public final static int CELL_HEADER_LEN = 3;
	public final static int CELL_VAR_HEADER_LEN = 5;
	public final static int CELL_PAYLOAD_LEN = CELL_LEN - CELL_HEADER_LEN;
	
	private final int circuitId;
	private final int command;
	private final ByteBuffer cellBuffer;
	
	/* Variable length cell constructor (ie: VERSIONS cells only) */
	private Cell(int circuitId, int command, int payloadLength) {
		this.circuitId = circuitId;
		this.command = command;
		this.cellBuffer = ByteBuffer.wrap(new byte[CELL_VAR_HEADER_LEN + payloadLength]);
		cellBuffer.putShort((short)circuitId);
		cellBuffer.put((byte)command);
		cellBuffer.putShort((short) payloadLength);
	}
	
	/* Fixed length cell constructor */
	protected Cell(int circuitId, int command) {
		this.circuitId = circuitId;
		this.command = command;
		this.cellBuffer = ByteBuffer.wrap(new byte[CELL_LEN]);
		cellBuffer.putShort((short) circuitId);
		cellBuffer.put((byte) command);
	}
	
	public int getCircuitId() {
		return circuitId;
	}
	
	public int getCommand() {
		return command;
	}
	
	public int getByte() {
		return cellBuffer.get() & 0xFF;
	}
	
	public int getByteAt(int index) {
		return cellBuffer.get(index) & 0xFF;
	}
	
	public int getShort() {
		return cellBuffer.getShort() & 0xFFFF;
	}
	
	public int getInt() {
		return cellBuffer.getInt();
	}
	
	public int getShortAt(int index) {
		return cellBuffer.getShort(index) & 0xFFFF;
	}
	
	public void getByteArray(byte[] buffer) {
		cellBuffer.get(buffer);
	}
	
	public int cellBytesConsumed() {
		return cellBuffer.position();
	}
	
	public int cellBytesRemaining() {
		return cellBuffer.remaining();
	}
	
	public void putByte(int value) {
		cellBuffer.put((byte) value);
	}
	
	public void putByteAt(int index, int value) {
		cellBuffer.put(index, (byte) value);
	}
	
	public void putShort(int value) {
		cellBuffer.putShort((short) value);
	}
	
	public void putShortAt(int index, int value) {
		cellBuffer.putShort(index, (short) value);
	}
	
	public void putInt(int value) {
		cellBuffer.putInt(value);
	}
	
	public void putByteArray(byte[] data) {
		cellBuffer.put(data);
	}

	public byte[] getCellBytes() {
		return cellBuffer.array();
	}
	
	public String toString() {
		return "Cell: circuit_id="+ circuitId +" command="+ command +" payload_len="+ cellBuffer.position();
	}

}
