package org.torproject.jtor.data.exitpolicy;

import org.torproject.jtor.TorException;
import org.torproject.jtor.TorParsingException;

public class PortRange {

	public static PortRange createFromString(String ports) {
		final String[] parts = ports.split("-");
		if(parts.length == 1) {
			return new PortRange(stringToPort(parts[0]));
		} else if(parts.length == 2) {
			return new PortRange(stringToPort(parts[0]), stringToPort(parts[1]));
		} else {
			throw new TorParsingException("Could not parse port range from string: " + ports);
		}
	}

	private static int stringToPort(String port) {
		try {
			final int portValue = Integer.parseInt(port);
			if(!isValidPort(portValue))
				throw new TorParsingException("Illegal port value: "+ port);
			return portValue;
		} catch(NumberFormatException e) {
			throw new TorParsingException("Could not parse port value: "+ port);
		}
	}
	private static final int MAX_PORT = 0xFFFF;
	public static final PortRange ALL_PORTS = new PortRange(1, MAX_PORT);
	private final int portStart;
	private final int portEnd;

	PortRange(int portValue) {
		this(portValue, portValue);
	}

	PortRange(int start, int end) {
		if(!isValidRange(start, end))
			throw new TorException("Invalid port range: "+ start +"-"+ end);
		portStart = start;
		portEnd = end;
	}

	private static boolean isValidRange(int start, int end) {
		return isValidPort(start) && isValidPort(end) && (start <= end);
	}

	private static boolean isValidPort(int port) {
		return port >= 0 && port <= MAX_PORT;
	}

	public boolean rangeContains(int port) {
		return port >= portStart && port <= portEnd;
	}

	public String toString() {
		if(portStart == portEnd)
			return Integer.toString(portStart);
		else
			return Integer.toString(portStart) + "-" + Integer.toString(portEnd);
	}

}
