package org.torproject.jtor.control;

import org.torproject.jtor.control.events.ControlEventQueue;

/**
 *
 * @author Merlijn Hofstra
 */
public abstract class ControlConnectionHandler extends Thread {

    protected boolean authenticated = false;
    protected boolean requestedProtocolinfo = false;
	protected ControlServer cs;
    protected ControlEventQueue eq = new ControlEventQueue();

    public boolean isAuthenticated() {
        return authenticated;
    }
    
    public void setAuthenticated(boolean authenticated) {
    	this.authenticated = authenticated;
    }
    
    public ControlServer getControlServer() {
    	return cs;
    }
    
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
