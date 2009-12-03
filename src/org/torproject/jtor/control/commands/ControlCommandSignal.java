package org.torproject.jtor.control.commands;

import org.torproject.jtor.TorConfig;
import org.torproject.jtor.control.ControlConnectionHandler;

public class ControlCommandSignal {

	/**
	 * Handles a signal from an incoming control connection
	 * @param in - the signal to be executed
	 * @return true if successful, false if signal not found/supported
	 */
	public static boolean handleSignal(ControlConnectionHandler cch, String in) {
		in = in.toLowerCase();
		TorConfig tc = cch.getControlServer().getTorConfig();

		if (in.equals("reload")) {
			if (tc.is__ReloadTorrcOnSIGHUP()) {
				tc.loadDefaults();
				tc.loadConf();
			}
		}

		else if (in.equals("shutdown")) {
			System.exit(0);
		}

		else if (in.equals("dump")) {
			// TODO what needs to be dumped here?
		}

		else if (in.equals("debug")) {
			// TODO we currently have just the debug level
		}

		else if (in.equals("halt")) {
			System.exit(0);
		}

		else if (in.equals("cleardnscache")) {
			java.security.Security.setProperty("networkaddress.cache.ttl" , "0");
			Thread.yield();
			System.gc();
			java.security.Security.setProperty("networkaddress.cache.ttl" , "-1");
		}

		else if (in.equals("newnym")) {
			java.security.Security.setProperty("networkaddress.cache.ttl" , "0");
			Thread.yield();
			System.gc();
			java.security.Security.setProperty("networkaddress.cache.ttl" , "-1");

			// TODO make new circuits and new connections must use these circuits
		}

		else {
			return false;
		}

		return true;
	}
}
