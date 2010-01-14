package org.torproject.jtor.logging.impl;

import java.util.HashMap;
import java.util.Map;

import org.torproject.jtor.logging.LogEntry;
import org.torproject.jtor.logging.LogManager;
import org.torproject.jtor.logging.LogReader;
import org.torproject.jtor.logging.Logger;

public class LogManagerImpl implements LogManager, LogReader{
	private final Map<String, LoggerImpl> loggers = new HashMap<String, LoggerImpl>();
	private final Object readerLock = new Object();
	private LogReader logReader = new DefaultConsoleLogReader();
		
	public synchronized Logger getLogger(String name) {
		if(!loggers.containsKey(name)) {
			loggers.put(name, new LoggerImpl(name, this));
		}
		return loggers.get(name);
	}

	public void logRaw(String message) {
		synchronized(readerLock) {
			if(logReader != null) logReader.logRaw(message);
		}
	}
	
	public void log(LogEntry entry) {
		synchronized(readerLock) {
			if(logReader != null) logReader.log(entry);
		}
	}
	
	public void setLogReader(LogReader reader) {
		synchronized(readerLock) {
			if(reader == null) {
				this.logReader = null;
				return;
			}

			if(logReader instanceof RingBufferLogReader) {
				RingBufferLogReader ringBufferReader = (RingBufferLogReader) logReader;
				for(LogEntry entry : ringBufferReader) {
					reader.log(entry);
				}
			}
			this.logReader = reader;
		}
	}
}
