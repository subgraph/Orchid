package org.torproject.jtor.control.impl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;

import org.torproject.jtor.Logger;
import org.torproject.jtor.Tor;
import org.torproject.jtor.TorConfig;
import org.torproject.jtor.config.impl.TorConfigImpl;
import org.torproject.jtor.control.ControlConnectionHandler;
import org.torproject.jtor.control.ControlServer;
import org.torproject.jtor.logging.ConsoleLogger;

/**
 *
 * @author Merlijn Hofstra
 */
public class ControlServerTCP extends ControlServer {

	private Vector<ControlConnectionHandler> connections = new Vector<ControlConnectionHandler>();

	public ControlServerTCP(Tor tor, TorConfig tc, Logger logger) {
		super(tor, tc, logger);
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
		ServerSocket ss = null;
		try {
			if (host != null) {
				ss = new ServerSocket(tc.getControlPort(), 0, host);
			} else {
				ss = new ServerSocket(tc.getControlPort());
			}
		} catch (IOException ex) {
			running = false;
		}
		while (running) {
			try {
				Socket s = ss.accept();
				ControlConnectionHandler cch = new ControlConnectionHandlerTCP(this, s);
				connections.add(cch);
				
				logger.debug("Opening new TCP Control Connection on port " + s.getLocalPort());
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
	
	public static void main(String[] arg) {
		Logger logger = new ConsoleLogger();
		TorConfig tc = new TorConfigImpl(logger);
		tc.loadDefaults();
		tc.loadConf();
		tc.setControlPort((short)9051);
		tc.saveConf();
		ControlServer cs = new ControlServerTCP(new Tor(), tc, logger);
		cs.startServer();
	}

}
