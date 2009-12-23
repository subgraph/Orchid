package org.torproject.jtor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A simple logger implementation that formats log messages and prints them to the console.
 */
public class ConsoleLogger implements Logger {
	private final DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss.SSS");
	public void debug(String message) {
		outputMessage(formatMessage("DEBUG", message));
	}

	public void error(String message) {
		outputMessage(formatMessage("ERROR", message));		
	}

	public void warn(String message) {
		outputMessage(formatMessage("WARNING", message));		
	}
	
	private void outputMessage(String message) {
		System.out.println(message);
	}
	private String formatMessage(String tag, String message) {
		return getTimestamp() +" "+ tag +": "+ message;
	}
	private String getTimestamp() {
		return "["+ dateFormat.format(new Date()) +"]";
		
	}

}
