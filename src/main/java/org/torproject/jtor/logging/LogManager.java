package org.torproject.jtor.logging;

public interface LogManager {
	Logger getLogger(String name);
	void setLogReader(LogReader reader);
	void logRaw(String message);

}
