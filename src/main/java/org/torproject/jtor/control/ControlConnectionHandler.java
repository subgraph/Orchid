package org.torproject.jtor.control;

import org.torproject.jtor.control.events.ControlEventQueue;

/**
 *
 * @author Merlijn Hofstra
 */
public abstract class ControlConnectionHandler extends Thread {

	private boolean authenticated = false;
	private boolean requestedProtocolinfo = false;
	private ControlEventQueue eq = new ControlEventQueue();

	public boolean isAuthenticated() {
		return authenticated;
	}

	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}

	public abstract ControlServer getControlServer();

	public boolean isRequestedProtocolinfo() {
		return requestedProtocolinfo;
	}

	public void setRequestedProtocolinfo(boolean requestedProtocolinfo) {
		this.requestedProtocolinfo = requestedProtocolinfo;
	}

	public ControlEventQueue getEventQueue() {
		return eq;
	}

	public abstract void disconnect();
	public abstract void write(String out);

}
