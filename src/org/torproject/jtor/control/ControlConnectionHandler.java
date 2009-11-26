package org.torproject.jtor.control;

/**
 *
 * @author merlijn
 */
public abstract class ControlConnectionHandler extends Thread {

    protected boolean authenticated = false;
    protected ControlServer cs;

    public boolean isAuthenticated() {
        return authenticated;
    }
    
    public ControlServer getControlServer() {
    	return cs;
    }

    public abstract void disconnect();
    public abstract void write(String out);

}
