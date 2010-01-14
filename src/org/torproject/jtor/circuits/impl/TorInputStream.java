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
	private volatile boolean isClosed;
	private boolean isEOF;
	TorInputStream(StreamImpl stream) {
		this.stream = stream;
		this.isClosed = false;
		incomingCells = new LinkedBlockingQueue<RelayCell>();
	}

	@Override
	public synchronized int read() throws IOException {
		checkOpen();

		while(currentBuffer == null || !currentBuffer.hasRemaining())
			fillBuffer();

		if(isEOF)
			return -1;

		synchronized(incomingCells) {
			availableBytes -= 1;
		}
		return currentBuffer.get() & 0xFF;
	}

	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	public synchronized int read(byte[] b, int off, int len) throws IOException {
		checkOpen();
		if(b == null)
			throw new NullPointerException();
		if( (off < 0) || (off > b.length) || (len < 0) ||
				((off + len) > b.length) || ((off + len) < 0))
			throw new IndexOutOfBoundsException();
		if(len == 0)
			return 0;

		while(!isEOF && (currentBuffer == null || !currentBuffer.hasRemaining()))
			fillBuffer();

		if(isEOF)
			return -1;

		int bytesRead = 0;
		synchronized(incomingCells) {
			while(availableBytes > 0) {
				if(currentBuffer.remaining() >= len) {
					currentBuffer.get(b, off, len);
					availableBytes -= len;
					bytesRead += len;
					return bytesRead;
				}
				int rem = currentBuffer.remaining();
				currentBuffer.get(b, off, rem);
				availableBytes -= rem;
				bytesRead += rem;
				len -= rem;
				off += rem;
				if(availableBytes > 0) {
					fillBuffer();
					if(isEOF)
						return bytesRead;
				}

			}
		}
		return bytesRead;
	}

	public int available() {
		synchronized(incomingCells) {
			return availableBytes;
		}
	}

	public void close() {
		isClosed = true;
		stream.close();
	}

	void addEndCell(RelayCell cell) {
		if(isClosed)
			return;
		incomingCells.add(cell);
	}

	void addInputCell(RelayCell cell) {
		if(isClosed)
			return;
		synchronized(incomingCells) {
			availableBytes += cell.cellBytesRemaining();
			incomingCells.add(cell);
		}
	}

	private void checkOpen() throws IOException {
		if (isClosed)
			throw new IOException("Input stream closed");
	}

	private void fillBuffer() throws IOException {
		checkOpen();

		if((currentBuffer != null && currentBuffer.hasRemaining()) || isEOF)
			return;

		try {
			final RelayCell nextCell = incomingCells.take();
			if(nextCell.getRelayCommand() == RelayCell.RELAY_END) {
				isEOF = true;
				return;
			}
			currentBuffer = nextCell.getPayloadBuffer();
			return;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IOException("Read interrupted");
		}

	}

	int unflushedCellCount() {
		return incomingCells.size();
	}

	public String toString() {
			return "TorInputStream stream="+ stream.getStreamId() +" node="+ stream.getTargetNode();
	}
}
