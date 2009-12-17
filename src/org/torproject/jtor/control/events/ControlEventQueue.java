package org.torproject.jtor.control.events;

import java.util.ArrayList;
import java.util.List;

import org.torproject.jtor.control.ControlConnectionHandler;

public class ControlEventQueue {
	
	private List<String> events = new ArrayList<String>();
	
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

}
