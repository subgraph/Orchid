package org.torproject.jtor.control.commands;

import org.torproject.jtor.TorConfig;
import org.torproject.jtor.config.impl.TorConfigParser;
import org.torproject.jtor.control.ControlConnectionHandler;

public class ControlCommandMapAddress {

	public static boolean handleMapAddress(ControlConnectionHandler cch, String in) {
		String host = in.substring(0, in.indexOf("="));
		String dest = in.substring(in.indexOf("=")+1);
		TorConfig tc = cch.getControlServer().getTorConfig();

		if (in.indexOf("=") == -1) {
			return false;
		}
		
		String[] maps = tc.getMapAddress();
		if (maps == null) {
			maps = new String[0];
		}

		boolean reset = false;
		for (int i = 0; i < maps.length; i++) { // reset any mappings with the hostname so it can be redefined
			if (maps[i].substring(0, in.indexOf("=")).equalsIgnoreCase(host)) {
				if (maps[i].substring(in.indexOf("=")+1).equalsIgnoreCase(dest)) {
					reset = true;
				}
				maps[i] = null;
			}
		}

		String[] newmaps = null;
		for (int i = 0; i < maps.length; i++) { // remove null values
			if (maps[i] != null) {
				newmaps = TorConfigParser.addToStringArray(newmaps, maps[i]);
			}
		}
		
		if (reset) { // address is removed, set new value and return
			tc.setMapAddress(newmaps);
			return true;
		}

		if (host.equals(".") || host.equals("0.0.0.0") || host.equals("::0")) { // null host
			String ip = "";
			boolean uniqueip = false;
			while (!uniqueip) {
				ip = "127";
				ip += "." + (int)(Math.random()*255);
				ip += "." + (int)(Math.random()*255);
				ip += "." + (int)(Math.random()*255);
				uniqueip = true;

				// check if host doesn't exist yet
				for(int i = 0; i < newmaps.length; i++) {
					if (maps[i].substring(0, in.indexOf("=")).equals(ip)) {
						uniqueip = false;
					}
				}
			}

			newmaps = TorConfigParser.addToStringArray(newmaps, ip + "=" + dest);
			tc.setMapAddress(newmaps);
		} else {
			newmaps = TorConfigParser.addToStringArray(newmaps, host + "=" + dest);
			tc.setMapAddress(newmaps);
		}
		
		return true;
	}
}
