package org.torproject.jtor.control;

import java.net.InetAddress;

import org.torproject.jtor.Logger;
import org.torproject.jtor.TorConfig;
import org.torproject.jtor.control.auth.ControlAuthenticator;

/**
 *
 * @author Merlijn Hofstra
 */
public abstract class ControlServer extends Thread {

    protected InetAddress host;
    protected TorConfig tc;
    protected Logger logger;
    protected boolean running = false;

    public abstract void startServer();
    public abstract void stopServer();
    public abstract void disconnectHandler(ControlConnectionHandler cch);
    public abstract String getProtocol();

    public ControlServer(TorConfig tc, Logger logger) {
        this.tc = tc;
        this.logger = logger;
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
    
    public Logger getLogger() {
    	return logger;
    }

}
