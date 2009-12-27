package org.torproject.jtor.logging.impl;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.torproject.jtor.logging.LogEntry;

public class OutputState {
	private final StringWriter stringWriter;
	private final PrintWriter printWriter;
	private final LogEntry entry;
	
	OutputState(LogEntry entry) {
		this.entry = entry;
		stringWriter = new StringWriter();
		printWriter = new PrintWriter(stringWriter);
	}
	
	LogEntry getEntry() {
		return entry;
	}
	
	void print(String s) {
		printWriter.print(s);
	}
	
	void println(String s) {
		printWriter.println(s);
	}
	
	void printException(Throwable exception) {
		if(exception.getMessage() != null) {
			printWriter.println(exception.getMessage());
		}
		exception.printStackTrace(printWriter);
	}
	
	public String toString() {
		printWriter.flush();
		return stringWriter.toString();
	}
}
