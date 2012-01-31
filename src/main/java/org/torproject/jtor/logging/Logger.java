package org.torproject.jtor.logging;

public interface Logger {
	void error(String message);
	
	void error(String message, Throwable exception);
	
	void warning(String message);
	
	void warning(String message, Throwable exception);
	
	void info(String message);
	
	void info(String message, Throwable exception);
	
	void debug(String message);
	
	void debug(String message, Throwable exception);
	
	void enableDebug();
	
	void disableDebug();
	
	LogManager getManager();

}
