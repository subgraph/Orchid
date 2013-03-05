package org.torproject.jtor.logging;

public interface LogReader {
	void log(LogEntry entry);
	void logRaw(String message);
}
