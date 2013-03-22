package org.torproject.jtor.control;

import java.net.InetAddress;

import org.torproject.jtor.TorConfig;
import org.torproject.jtor.control.auth.ControlAuthenticator;
import org.torproject.jtor.directory.Directory;

/**
 *
 * @author Merlijn Hofstra
 */
public abstract class ControlServer extends Thread {

    protected InetAddress host;
    protected TorConfig tc;
    private final Directory directory;
    protected boolean running = false;

    public abstract void startServer();
    public abstract void stopServer();
    public abstract void disconnectHandler(ControlConnectionHandler cch);
    public abstract String getProtocol();

    public ControlServer(Directory directory, TorConfig tc) {
    	this.directory = directory;
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
  
    public Directory getDirectory() {
    	return directory;
    }
}
