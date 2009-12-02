package org.torproject.jtor.control;

/**
 *
 * @author merlijn
 */
public abstract class ControlConnectionHandler extends Thread {

    protected boolean authenticated = false;
    boolean requestedProtocolinfo = false;
	protected ControlServer cs;

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

    public abstract void disconnect();
    public abstract void write(String out);

}
