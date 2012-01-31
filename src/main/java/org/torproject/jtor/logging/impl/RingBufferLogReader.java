package org.torproject.jtor.logging.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.torproject.jtor.logging.LogEntry;
import org.torproject.jtor.logging.LogReader;

public class RingBufferLogReader implements LogReader, Iterable<LogEntry> {
	private final static int SIZE = 100;
	final private List<LogEntry> ringBuffer = new ArrayList<LogEntry>(100);
	int writeIndex; 
	int readIndex;
	
	public void log(LogEntry entry) {
		ringBuffer.add(writeIndex, entry);
		writeIndex = (writeIndex + 1) % SIZE;
		if(readIndex == writeIndex) {
			readIndex = (readIndex + 1) % SIZE;
		}		
	}
	public void logRaw(String message) {
	}

	public static class BufferIterator implements Iterator<LogEntry> {
		private List<LogEntry> buffer;
		private int ridx;
		private int widx;
		BufferIterator(List<LogEntry> buffer, int ridx, int widx) {
			this.buffer = new ArrayList<LogEntry>(buffer);
			this.ridx = ridx;
			this.widx = widx;
		}
		public boolean hasNext() {
			return ridx != widx;
		}
		public LogEntry next() {
			final LogEntry entry = buffer.get(ridx);
			ridx = (ridx + 1) % SIZE;
			return entry;
		}
		public void remove() {
			throw new UnsupportedOperationException();			
		}
	}

	public Iterator<LogEntry> iterator() {
		return new BufferIterator(ringBuffer, readIndex, writeIndex);
	}
}
