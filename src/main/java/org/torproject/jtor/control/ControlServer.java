package org.torproject.jtor.control;

import java.net.InetAddress;

import org.torproject.jtor.TorConfig;
import org.torproject.jtor.control.auth.ControlAuthenticator;
import org.torproject.jtor.directory.Directory;
import org.torproject.jtor.logging.LogManager;
import org.torproject.jtor.logging.Logger;

/**
 *
 * @author Merlijn Hofstra
 */
public abstract class ControlServer extends Thread {

    protected InetAddress host;
    protected TorConfig tc;
    private final Directory directory;
    protected Logger logger;
    protected boolean running = false;

    public abstract void startServer();
    public abstract void stopServer();
    public abstract void disconnectHandler(ControlConnectionHandler cch);
    public abstract String getProtocol();

    public ControlServer(Directory directory, TorConfig tc, LogManager logManager) {
    	this.directory = directory;
        this.tc = tc;
        this.logger = logManager.getLogger("controller");
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
    
    public Directory getDirectory() {
    	return directory;
    }
}
