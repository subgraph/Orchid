package org.torproject.jtor.logging.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.torproject.jtor.logging.LogEntry;
import org.torproject.jtor.logging.LogEntry.LogLevel;
import org.torproject.jtor.logging.LogReader;

public class DefaultConsoleLogReader implements LogReader {
	private final DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss.SSS");

	public void log(LogEntry entry) {
		final OutputState out = new OutputState(entry);
		
		addBanner(out);
		out.println(entry.getMessage());
		addException(out);
		printToConsole(out);
		
	}

	public void logRaw(String message) {
		System.out.println(message);
	}
	
	private void addBanner(OutputState out) {
		final LogEntry entry = out.getEntry();
		out.print(getTimestamp());
		switch(entry.getLevel()) {
		case DEBUG:
			out.print("DEBUG");
			break;
		case INFO:
			out.print("INFO");
			break;
		case WARNING:
			out.print("WARN");
			break;
		case ERROR:
			out.print("ERROR");
			break;
		}
		
		out.print(" (" + entry.getComponent() + ") : ");
	}
	
	private void addException(OutputState out) {
		final LogEntry entry = out.getEntry();
		if(entry.getException() != null) {
			out.printException(entry.getException());
		}
	}
	
	private String getTimestamp() {
		return "["+ dateFormat.format(new Date()) +"] ";
	}
	
	private void printToConsole(OutputState out) {
		final LogEntry entry = out.getEntry();
		if(entry.getLevel() == LogLevel.ERROR) {
			System.err.print(out);
		} else {
			System.out.print(out);
		}
	}


}
