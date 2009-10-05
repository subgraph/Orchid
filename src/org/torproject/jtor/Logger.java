package org.torproject.jtor;

public interface Logger {
	/**
	 * Log a message at the 'debug' logging level.
	 * 
	 * @param message The message to log.
	 */
	void debug(String message);
	
	/**
	 * Log a message at the 'warn' logging level.
	 * 
	 * @param message The message to log.
	 */
	void warn(String message);
	
	/**
	 * Log a message at the 'error' logging level.
	 * 
	 * @param message The message to log.
	 */
	void error(String message);
}
