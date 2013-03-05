package org.torproject.jtor.control.impl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.torproject.jtor.TorConfig;
import org.torproject.jtor.control.ControlConnectionHandler;
import org.torproject.jtor.control.ControlServer;
import org.torproject.jtor.directory.Directory;
import org.torproject.jtor.events.Event;
import org.torproject.jtor.events.EventHandler;
import org.torproject.jtor.logging.LogManager;

/**
 *
 * @author Merlijn Hofstra
 */
public class ControlServerTCP extends ControlServer implements EventHandler {

	private List<ControlConnectionHandler> connections = new Vector<ControlConnectionHandler>();
	private ServerSocket ss;

	public ControlServerTCP(Directory directory, TorConfig tc, LogManager logManager) {
		super(directory, tc, logManager);
	}

	@Override
	public void startServer() {
		if (getTorConfig().getControlPort() > 0 && !isRunning()) {
			setRunning(true);
			this.start();
		}
	}

	@Override
	public void run() {
		try {
			if (getInetAddress() != null) {
				ss = new ServerSocket(getTorConfig().getControlPort(), 0, getInetAddress());
			} else {
				ss = new ServerSocket(getTorConfig().getControlPort());
			}
		} catch (IOException ex) {
			setRunning(false);
		}

		getTorConfig().registerConfigChangedHandler(this);

		while (isRunning()) {
			try {
				Socket s = ss.accept();
				ControlConnectionHandler cch = new ControlConnectionHandlerTCP(this, s);
				connections.add(cch);

				getLogger().debug("Opening new TCP Control Connection on port " + s.getLocalPort());
			} catch (Throwable t) {}
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public void stopServer() {
		setRunning(false);
		this.interrupt();
		Iterator i = connections.iterator();
		while (i.hasNext()) {
			((ControlConnectionHandler)i.next()).disconnect();
		}
		getTorConfig().unregisterConfigChangedHandler(this);
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
		if (getTorConfig().getControlPort() != ss.getLocalPort()) {
			stopServer();
			startServer();
		}
	}
}
