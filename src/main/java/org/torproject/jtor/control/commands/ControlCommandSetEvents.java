package org.torproject.jtor.control.commands;

import org.torproject.jtor.control.ControlConnectionHandler;
import org.torproject.jtor.control.KeyNotFoundException;
import org.torproject.jtor.directory.Directory;

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

		final Directory directory = cch.getControlServer().getDirectory();
		cch.getEventQueue().resetAllHandlers(directory);
		
		for (String event : eventtypes) {
			if (event.equals("newconsensus")) {
				cch.getEventQueue().addNewConsensusHandler(directory);
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
