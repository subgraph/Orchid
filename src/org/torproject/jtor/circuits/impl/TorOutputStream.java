package org.torproject.jtor.circuits.impl;

import java.io.IOException;
import java.io.OutputStream;

import org.torproject.jtor.circuits.cells.RelayCell;

public class TorOutputStream extends OutputStream {

	private final StreamImpl stream;
	private RelayCell currentOutputCell;
	
	TorOutputStream(StreamImpl stream) {
		this.stream = stream;
	}
	
	private void flushCurrentOutputCell() {
		if(currentOutputCell != null && currentOutputCell.cellBytesConsumed() > RelayCell.HEADER_SIZE) 
			stream.getCircuit().sendRelayCell(currentOutputCell);
		currentOutputCell = new RelayCellImpl(stream.getTargetNode(), stream.getCircuit().getCircuitId(),
				stream.getStreamId(), RelayCell.RELAY_DATA);
	}
	@Override
	public void write(int b) throws IOException {
		if(currentOutputCell == null || currentOutputCell.cellBytesRemaining() == 0)
			flushCurrentOutputCell();
		currentOutputCell.putByte(b);		
	}

	public void write(byte[] data, int offset, int length) {
		if(currentOutputCell == null || currentOutputCell.cellBytesRemaining() == 0)
			flushCurrentOutputCell();
		
		while(length > 0) {
			if(length < currentOutputCell.cellBytesRemaining()) {
				currentOutputCell.putByteArray(data, offset, length);
				return;
			}
			final int writeCount = currentOutputCell.cellBytesRemaining();
			currentOutputCell.putByteArray(data, offset, writeCount);
			flushCurrentOutputCell();
			offset += writeCount;
			length -= writeCount;
		}
	}
	
	public void flush() {
		flushCurrentOutputCell();
	}
	
	public void close() {
		stream.close();
	}
}
