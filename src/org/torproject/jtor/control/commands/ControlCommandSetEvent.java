package org.torproject.jtor.control.commands;

import org.torproject.jtor.control.ControlConnectionHandler;
import org.torproject.jtor.control.KeyNotFoundException;

public class ControlCommandSetEvent {
	
	public static void handleSetEvent(ControlConnectionHandler cch, String in) throws KeyNotFoundException {
		String [] eventtypes = in.toLowerCase().split(" ");
		for (String event : eventtypes) {
			if (!verifyEvent(event)) {
				throw new KeyNotFoundException();
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
