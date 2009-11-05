package org.torproject.jtor.circuits.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.torproject.jtor.circuits.cells.RelayCell;

public class TorInputStream extends InputStream {
	
	private final BlockingQueue<RelayCell> incomingCells;
	private final StreamImpl stream;
	private ByteBuffer currentBuffer;
	private int availableBytes;
	private volatile boolean closed;
	
	TorInputStream(StreamImpl stream) {
		this.stream = stream;
		this.closed = false;
		incomingCells = new LinkedBlockingQueue<RelayCell>();
	}
	
	@Override
	public int read() throws IOException {
		if(closed)
			throw new IOException("Stream is closed");
		
		while(currentBuffer == null || !currentBuffer.hasRemaining())
			fillBuffer();
		synchronized(incomingCells) {
			availableBytes -= 1;
		}
		return currentBuffer.get() & 0xFF;
	}
	
	public int available() {
		return availableBytes;
	}
	
	public void close() {
		closed = true;
		incomingCells.clear();
		stream.close();
	}
	
	void addInputCell(RelayCell cell) {
		if(closed)
			return;
		synchronized(incomingCells) {
			availableBytes += cell.cellBytesRemaining();
		}
		incomingCells.add(cell);
	}
	
	private void checkOpen() throws IOException {
	      if (closed)
	    	  throw new IOException("Input stream closed");
	}
	
	private void fillBuffer() throws IOException {
		checkOpen();
		
		if(currentBuffer != null && currentBuffer.hasRemaining())
			return;
		
		try {
			final RelayCell nextCell = incomingCells.take();
			currentBuffer = nextCell.getPayloadBuffer();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IOException("Read interrupted");
		}
		
	}

}
