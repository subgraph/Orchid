package org.torproject.jtor.control.impl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;

import org.torproject.jtor.TorConfig;
import org.torproject.jtor.control.ControlConnectionHandler;
import org.torproject.jtor.control.ControlServer;
import org.torproject.jtor.directory.Directory;
import org.torproject.jtor.events.Event;
import org.torproject.jtor.events.EventHandler;

/**
 *
 * @author Merlijn Hofstra
 */
public class ControlServerTCP extends ControlServer implements EventHandler {
	private final static Logger logger = Logger.getLogger(ControlServerTCP.class.getName());
	private Vector<ControlConnectionHandler> connections = new Vector<ControlConnectionHandler>();
	private ServerSocket ss;
	
	public ControlServerTCP(Directory directory, TorConfig tc) {
		super(directory, tc);
	}

	@Override
	public void startServer() {
		if (tc.getControlPort() > 0 && !running) {
			running = true;
			this.start();
		}
	}

	@Override
	public void run() {
		try {
			if (host != null) {
				ss = new ServerSocket(tc.getControlPort(), 0, host);
			} else {
				ss = new ServerSocket(tc.getControlPort());
			}
		} catch (IOException ex) {
			running = false;
		}
		
		tc.registerConfigChangedHandler(this);
		
		while (running) {
			try {
				Socket s = ss.accept();
				ControlConnectionHandler cch = new ControlConnectionHandlerTCP(this, s);
				connections.add(cch);
				
				logger.fine("Opening new TCP Control Connection on port " + s.getLocalPort());
			} catch (Throwable t) {}
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public void stopServer() {
		running = false;
		this.interrupt();
		Iterator i = connections.iterator();
		while (i.hasNext()) {
			((ControlConnectionHandler)i.next()).disconnect();
		}
		tc.unregisterConfigChangedHandler(this);
	}

	public void disconnectHandler(ControlConnectionHandler cch) {
		if (connections.remove(cch)) {
			cch.disconnect();
		}
	}

	@Override
	public String getProtocol() {
		return "TCP";
	}

	/*
	 * Catches configuration updates
	 * (non-Javadoc)
	 * @see org.torproject.jtor.events.EventHandler#handleEvent(org.torproject.jtor.events.Event)
	 */
	public void handleEvent(Event event) {
		if (tc.getControlPort() != ss.getLocalPort()) {
			stopServer();
			startServer();
		}
	}
}
