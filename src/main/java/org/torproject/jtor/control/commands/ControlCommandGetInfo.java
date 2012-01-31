package org.torproject.jtor.control.commands;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.torproject.jtor.Tor;
import org.torproject.jtor.TorConfig;
import org.torproject.jtor.control.ControlConnectionHandler;
import org.torproject.jtor.control.FeatureNotSupportedException;
import org.torproject.jtor.control.KeyNotFoundException;

public class ControlCommandGetInfo {

	public static String handleGetInfo(ControlConnectionHandler cch, String key) throws KeyNotFoundException, FeatureNotSupportedException {
		key = key.toLowerCase();
		TorConfig tc = cch.getControlServer().getTorConfig();
		
		if (key.equals("version")) {
			return Tor.getVersion();
		}
		
		else if (key.equals("config-file")) {
			return tc.getDataDirectory() + File.separator + tc.getConfigFile();
		}
		
		else if (key.equals("address")) {
			try {
				InetAddress ip =InetAddress.getLocalHost();
				if (ip.isAnyLocalAddress() || ip.isLoopbackAddress()) {
					throw new FeatureNotSupportedException();
				}
				
				if (ip.getHostAddress().startsWith("192.168") || ip.getHostAddress().startsWith("10.") || 
						ip.getHostAddress().matches("^172\\.(1[6-9]]|2[0-9]|3[01])\\..*")) {
					throw new FeatureNotSupportedException();
				}
				
				return ip.getHostAddress();
			} catch (UnknownHostException e) {
				throw new FeatureNotSupportedException();
			}
		}
		
		else if (key.equals("fingerprint")) {
			throw new FeatureNotSupportedException();
		}
		
		else if (key.equals("events/names")) {
			String ret = "";
			for (String event : ControlCommandSetEvents.supportedEvents) {
				ret += event + " ";
			}
			return ret.replaceAll("(.*)\\s+$", "$1"); // strip trailing whitespace
		}
		
		else if (key.equals("features/names")) {
			return ""; // TODO this probably needs a better solution
		}
		
		throw new KeyNotFoundException();
	}
}
