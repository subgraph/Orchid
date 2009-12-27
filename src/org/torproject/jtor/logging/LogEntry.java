package org.torproject.jtor.logging;

public interface LogEntry {
	enum LogLevel { ERROR, WARNING, INFO, DEBUG };
	LogLevel getLevel();
	String getComponent();
	String getMessage();
	Throwable getException();
}
