package org.torproject.jtor.circuits.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.torproject.jtor.circuits.cells.Cell;

class CellImpl implements Cell {

	static CellImpl createCell(int circuitId, int command) {
		return new CellImpl(circuitId, command);
	}
	
	static CellImpl createVarCell(int circuitId, int command, int payloadLength) {
		return new CellImpl(circuitId, command, payloadLength);
	}
	
	static CellImpl readFromInputStream(InputStream input) throws IOException {
		final ByteBuffer header = readHeaderFromInputStream(input);
		final int circuitId = header.getShort() & 0xFFFF;
		final int command = header.get() & 0xFF;
		if(command == VERSIONS)
			return readVarCell(circuitId, command, input);
		final CellImpl cell = new CellImpl(circuitId, command);
		readAll(input, cell.getCellBytes(), CELL_HEADER_LEN, CELL_PAYLOAD_LEN);
		return cell;
	}
	
	private static ByteBuffer readHeaderFromInputStream(InputStream input) throws IOException {
		final byte[] cellHeader = new byte[CELL_HEADER_LEN];
		readAll(input, cellHeader);
		return ByteBuffer.wrap(cellHeader);
	}
	
	private static CellImpl readVarCell(int circuitId, int command, InputStream input) throws IOException {
		final byte[] lengthField = new byte[2];
		readAll(input, lengthField);
		final int length = ((lengthField[0] & 0xFF) << 8) | (lengthField[1] & 0xFF);
		CellImpl cell = new CellImpl(circuitId, command, length);
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
	
	private final int circuitId;
	private final int command;
	protected final ByteBuffer cellBuffer;
	
	/* Variable length cell constructor (ie: VERSIONS cells only) */
	private CellImpl(int circuitId, int command, int payloadLength) {
		this.circuitId = circuitId;
		this.command = command;
		this.cellBuffer = ByteBuffer.wrap(new byte[CELL_VAR_HEADER_LEN + payloadLength]);
		cellBuffer.putShort((short)circuitId);
		cellBuffer.put((byte)command);
		cellBuffer.putShort((short) payloadLength);
		cellBuffer.mark();
	}
	
	/* Fixed length cell constructor */
	protected CellImpl(int circuitId, int command) {
		this.circuitId = circuitId;
		this.command = command;
		this.cellBuffer = ByteBuffer.wrap(new byte[CELL_LEN]);
		cellBuffer.putShort((short) circuitId);
		cellBuffer.put((byte) command);
		cellBuffer.mark();
	}
	
	protected CellImpl(byte[] rawCell) {
		this.cellBuffer = ByteBuffer.wrap(rawCell);
		this.circuitId = cellBuffer.getShort() & 0xFFFF;
		this.command = cellBuffer.get() & 0xFF;
		cellBuffer.mark();
	}
	
	public int getCircuitId() {
		return circuitId;
	}
	
	public int getCommand() {
		return command;
	}
	
	public void resetToPayload() {
		cellBuffer.reset();
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

	public void putByteArray(byte[] data, int offset, int length) {
		cellBuffer.put(data, offset, length);
	}
	
	public byte[] getCellBytes() {
		return cellBuffer.array();
	}
	
	public String toString() {
		return "Cell: circuit_id="+ circuitId +" command="+ command +" payload_len="+ cellBuffer.position();
	}

}
