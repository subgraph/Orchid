package org.torproject.jtor.control.impl;

import java.net.Socket;

import org.torproject.jtor.TorConfig;
import org.torproject.jtor.control.ControlConnectionHandler;

/**
 *
 * @author merlijn
 */
public class ControlCommandParser {

	private ControlConnectionHandler cch;
	
	public ControlCommandParser(ControlConnectionHandler cch) {
		this.cch = cch;
	}

    public boolean verifyCommand(String in) {
        return true;
    }
    
    public boolean execute(String in) {
    	if (verifyCommand(in)) {
    		String command = in.substring(0, in.indexOf(" ")).toLowerCase();
    		String args = in.substring(in.indexOf(" ", 0)+1);
    		args = removeQuotes(args);
    		
    		if (command.equals("setconf")) {
    			String[] confs = args.split(" ");
    			for (int i = 0; i < confs.length; i++) {
    				String key = confs[i].substring(0, confs[i].indexOf("="));
    				String value = confs[i].substring(confs[i].indexOf("=")+1);
    				cch.getControlServer().getTorConfig().setConf(key, value);
    			}
    			cch.write("250 configuration values set");
    		}
    		
    		if (command.equals("getconf")) {
    			String[] confs = args.split(" ");
    			for (int i = 0; i < confs.length; i++) {
    				String[] values = cch.getControlServer().getTorConfig().getConfArray(confs[i]);
    				for (int j = 0; j < values.length; j++) {
    					cch.write("250 " + confs[i] + "=" + values[j]);
    				}
    			}
    		}
    	}
    	return true;
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
