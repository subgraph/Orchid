package org.torproject.jtor.logging;

import java.util.ArrayList;
import java.util.List;

import org.torproject.jtor.Logger;

public class MultiLogger implements Logger {
	
	List<Logger> loggers = new ArrayList<Logger>();

	public void debug(String message) {
		for (int i = 0; i < loggers.size(); i++) {
			loggers.get(i).debug(message);
		}
	}

	public void error(String message) {
		for (int i = 0; i < loggers.size(); i++) {
			loggers.get(i).error(message);
		}
	}

	public void warn(String message) {
		for (int i = 0; i < loggers.size(); i++) {
			loggers.get(i).warn(message);
		}
	}

}
