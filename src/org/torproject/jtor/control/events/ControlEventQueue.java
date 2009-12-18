package org.torproject.jtor.control.events;

import java.util.ArrayList;
import java.util.List;

import org.torproject.jtor.control.ControlConnectionHandler;
import org.torproject.jtor.directory.Directory;

public class ControlEventQueue {
	
	private List<String> events = new ArrayList<String>();
	
	private ControlEventHandler newconsensus;
	
	public void addMessage(final String message) {
		synchronized (this) {
			events.add(message);
		}
	}
	
	public synchronized void writeQueue(ControlConnectionHandler cch) {
		synchronized (this) {
			String[] messages = new String[events.size()];
			events.toArray(messages);
			for (String msg : messages) {
				cch.write(msg);
			}
			events.clear();
		}
	}
	
	public void resetAllHandlers(Directory dir) {
		dir.unregisterConsensusChangedHandler(newconsensus);
		
		newconsensus = null;
		
		System.gc();
	}
	
	public void addNewConsensusHandler(Directory dir) {
		if (newconsensus == null) {
			newconsensus = new NewConsensusHandler(this);
			dir.registerConsensusChangedHandler(newconsensus);
		}
	}

}
