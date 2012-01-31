package org.torproject.jtor.logging.impl;

import org.torproject.jtor.logging.LogManager;
import org.torproject.jtor.logging.LogReader;
import org.torproject.jtor.logging.Logger;
import org.torproject.jtor.logging.LogEntry.LogLevel;

public class LoggerImpl implements Logger {
	private final String name;
	private final LogReader reader;
	private final LogManager manager;
	private boolean debugEnabled;
	
	LoggerImpl(String name, LogManagerImpl manager) {
		this.name = name;
		this.reader = manager;
		this.manager = manager;
		debugEnabled = false;
	}

	public LogManager getManager() {
		return manager;
	}
	
	public void debug(String message) {
		debug(message, null);
	}

	public void debug(String message, Throwable exception) {
		if(debugEnabled) {
			log(LogLevel.DEBUG, message, exception);
		}
	}

	public void enableDebug() {
		debugEnabled = true;
	}
	
	public void disableDebug() {
		debugEnabled = false;
	}
	
	public void error(String message) {
		error(message, null);		
	}

	public void error(String message, Throwable exception) {
		log(LogLevel.ERROR, message, exception);
	}

	public void info(String message) {
		info(message, null);		
	}

	public void info(String message, Throwable exception) {
		log(LogLevel.INFO, message, exception);		
	}

	public void warning(String message) {
		warning(message, null);		
	}

	public void warning(String message, Throwable exception) {
		log(LogLevel.WARNING, message, exception);		
	}
	
	private void log(LogLevel level, String message, Throwable exception) {
		if(reader != null) {
			reader.log(new LogEntryImpl(level, name, message, exception));
		}
	}
}
