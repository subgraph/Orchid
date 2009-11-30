package org.torproject.jtor.control;

import java.net.InetAddress;
import org.torproject.jtor.TorConfig;
import org.torproject.jtor.control.auth.ControlAuthenticator;

/**
 *
 * @author Merlijn Hofstra
 */
public abstract class ControlServer extends Thread {

    protected InetAddress host;
    protected TorConfig tc;
    protected boolean running = false;

    public abstract void startServer();
    public abstract void stopServer();
    public abstract void disconnectHandler(ControlConnectionHandler cch);
    public abstract String getProtocol();

    public ControlServer(TorConfig tc) {
        this.tc = tc;
        if (tc.isCookieAuthentication()) {
        	ControlAuthenticator.writeCookie(tc);
        }
    }
    
    public void setInetAddress(InetAddress host) {
        this.host = host;
    }

    public boolean isRunning() {
        return running;
    }
    
    public TorConfig getTorConfig() {
    	return tc;
    }

}
