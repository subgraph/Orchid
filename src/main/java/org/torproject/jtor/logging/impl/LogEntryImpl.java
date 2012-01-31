package org.torproject.jtor.logging.impl;

import org.torproject.jtor.logging.LogEntry;

public class LogEntryImpl implements LogEntry {
	private final LogLevel level;
	private final String component;
	private final String message;
	private final Throwable exception;
	
	LogEntryImpl(LogLevel level, String component, String message, Throwable exception) {
		this.level = level;
		this.component = component;
		this.message = message;
		this.exception = exception;
	}

	public Throwable getException() {
		return exception;
	}

	public LogLevel getLevel() {
		return level;
	}

	public String getMessage() {
		return message;
	}
	
	public String getComponent() {
		return component;
	}

}
