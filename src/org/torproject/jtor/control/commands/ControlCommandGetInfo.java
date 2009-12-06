package org.torproject.jtor.control.commands;

import org.torproject.jtor.Tor;
import org.torproject.jtor.control.ControlConnectionHandler;
import org.torproject.jtor.control.KeyNotFoundException;

public class ControlCommandGetInfo {

	public static String handleGetInfo(ControlConnectionHandler cch, String key) throws KeyNotFoundException {
		key = key.toLowerCase();
		if (key.equals("version")) {
			return Tor.version;
		}
		
		throw new KeyNotFoundException();
	}
}
