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

	private InetAddress inetAddress;
	private TorConfig tc;
	private final Directory directory;
	private Logger logger;
	private boolean running = false;

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

	protected InetAddress getInetAddress() {
		return inetAddress;
	}

	public void setInetAddress(InetAddress inetAddress) {
		this.inetAddress = inetAddress;
	}

	public boolean isRunning() {
		return running;
	}

	protected void setRunning(boolean running) {
		this.running = running;
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
