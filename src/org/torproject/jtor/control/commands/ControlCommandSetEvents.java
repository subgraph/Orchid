package org.torproject.jtor.control.commands;

import org.torproject.jtor.Tor;
import org.torproject.jtor.control.ControlConnectionHandler;
import org.torproject.jtor.control.KeyNotFoundException;

public class ControlCommandSetEvents {
	
	public static void handleSetEvent(ControlConnectionHandler cch, String in) throws KeyNotFoundException {
		String[] eventtypes = in.toLowerCase().split(" ");
		if (eventtypes[0].equals("extended")) {
			String[] temp = new String[eventtypes.length-1];
			System.arraycopy(eventtypes, 1, temp, 0, temp.length);
			eventtypes = temp;
		}
		
		for (String event : eventtypes) {
			if (!verifyEvent(event)) {
				throw new KeyNotFoundException();
			}
		}
		
		Tor tor = cch.getControlServer().getTor();
		cch.getEventQueue().resetAllHandlers(tor.getDirectory());
		
		for (String event : eventtypes) {
			if (event.equals("newconsensus")) {
				cch.getEventQueue().addNewConsensusHandler(tor.getDirectory());
			}
		}
		
	}
	
	public static String[] supportedEvents = { 
		"CIRC" , "STREAM" , "ORCONN" , "BW" , "DEBUG" ,
        "INFO" , "NOTICE" , "WARN" , "ERR" , "NEWDESC" , "ADDRMAP" ,
        "AUTHDIR_NEWDESCS" , "DESCCHANGED" , "STATUS_GENERAL" ,
        "STATUS_CLIENT" , "STATUS_SERVER" , "GUARD" , "NS" , "STREAM_BW" ,
        "CLIENTS_SEEN" , "NEWCONSENSUS" 
    };

	
	private static boolean verifyEvent (String event) {
		for (String sup : supportedEvents) {
			if (event.equalsIgnoreCase(sup)) {
				return true;
			}
		}
		
		return false;
	}

}
