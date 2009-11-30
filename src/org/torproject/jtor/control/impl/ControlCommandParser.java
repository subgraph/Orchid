package org.torproject.jtor.control.impl;

import java.util.HashMap;
import java.util.Iterator;

import org.torproject.jtor.control.ControlConnectionHandler;
import org.torproject.jtor.control.KeyNotFoundException;
import org.torproject.jtor.control.commands.*;

/**
 *
 * @author Merlijn Hofstra
 */
public class ControlCommandParser {

	private ControlConnectionHandler cch;

	public ControlCommandParser(ControlConnectionHandler cch) {
		this.cch = cch;
	}

	@SuppressWarnings("unchecked")
	public void execute(String in) {
		String command = in.substring(0, in.indexOf(" ")).toLowerCase();
		String args = in.substring(in.indexOf(" ", 0)+1);
		args = removeQuotes(args);

		if (command.equals("setconf")) {
			ControlCommandSetConf.handleSetConf(cch, args);
		}

		else if (command.equals("resetconf")) {
			ControlCommandSetConf.handleSetConf(cch, args);
		}

		else if (command.equals("getconf")) {
			String[] confs = args.split(" ");
			HashMap pairs = new HashMap(); 
			for (int i = 0; i < confs.length; i++) {
				try {
					if (confs[i].toLowerCase().equals("hiddenserviceoptions")) {
						pairs.put("HiddenServiceDir", ControlCommandGetConf.handleGetConf(cch, "HiddenServiceDir"));
						pairs.put("HiddenServicePort", ControlCommandGetConf.handleGetConf(cch, "HiddenServicePort"));
						pairs.put("HiddenServiceNodes", ControlCommandGetConf.handleGetConf(cch, "HiddenServiceNodes"));
						pairs.put("HiddenServiceExcludeNodes", ControlCommandGetConf.handleGetConf(cch, "HiddenServiceExcludeNodes"));
					} else {
						String value = ControlCommandGetConf.handleGetConf(cch, confs[i]);
						pairs.put(confs[i], value);
					}
				} catch (KeyNotFoundException e) {
					cch.write("552 unknown configuration keyword");
				}
			}

			// reply with key=value pairs
			Iterator it = pairs.keySet().iterator();
			while (it.hasNext()) {
				String key = (String)it.next();
				String val = ((String)pairs.get(key));

				if (val == null) {
					cch.write("250 " + key);
				}
				String[] vals = val.split("\n");
				for (int i = 0; i < vals.length; i++) {
					if (vals[i] == null || vals[i].equals("")) {
						// default value
						cch.write("250 " + key);
					} else {
						cch.write("250 " + key + "=" + vals[i]);
					}
				}
			}
		}

		else if (command.equals("signal")) {
			if (ControlCommandSignal.handleSignal(cch, args)) {
				cch.write("250 OK");
			} else {
				cch.write("552 Unrecognized signal");
			}
		}
		
		else if (command.equals("mapaddress")) {
			String[] maps = args.split(" ");
			for (int i = 0; i < maps.length; i++) {
				ControlCommandMapAddress.handleMapAddress(cch, maps[i]);
			}
		}
	}

	/** Removes any unescaped quotes from a given string */
	public static String removeQuotes(String in) {
		int index = 0;
		while (index < in.length()) {
			index = in.indexOf("\"", index);
			if (!in.substring(index-1, index).equals("\\")) {
				//remove the quote as it's not escaped
				in = in.substring(0, index) + in.substring(index+1);
			}
		}
		return in;
	}
}
