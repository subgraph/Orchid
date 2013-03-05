package org.torproject.jtor.control.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import org.torproject.jtor.control.ControlConnectionHandler;
import org.torproject.jtor.control.ControlServer;

/**
 *
 * @author Merlijn Hofstra
 */
public class ControlConnectionHandlerTCP extends ControlConnectionHandler {

	private ControlServer cs;
	private Socket s;
	private boolean running = false;

	public ControlConnectionHandlerTCP(ControlServer cs, Socket s) {
		this.s = s;
		this.cs = cs;
		this.running = true;
		start();
	}

	public ControlServer getControlServer() {
		return cs;
	}

	@Override
	public void run() {
		InputStreamReader inCon = null;
		BufferedReader in = null;
		try {
			inCon = new InputStreamReader(s.getInputStream());
			in = new BufferedReader(inCon);

			while (running) {
				String recv = in.readLine();

				recv.length(); // trigger NullPointerException
				cs.getLogger().debug("Control Connection TCP: received " + recv);

				getEventQueue().writeQueue(this);

				ControlCommandParser.execute(this, recv);
			}

		} catch (IOException ex) {
			cs.getLogger().debug("Control Connection TCP: IOException during receiving");
		} catch (NullPointerException e) {
			// may happen upon disconnect
		} finally {
			try {
				disconnect();
				if (in != null) {
					in.close();
				} else if (inCon != null) {
					inCon.close();
				}
			} catch (IOException ex) {}
		}

	}

	public void write(String w) {
		try {
			OutputStreamWriter out = new OutputStreamWriter(s.getOutputStream());

			cs.getLogger().debug("Control Connection TCP:  sending " + w);

			out.write(w + "\r\n");
			out.flush();
		} catch (IOException ex) {
			cs.getLogger().debug("Control Connection TCP: IOException during sending");
			disconnect();
		}

	}

	@Override
	public synchronized void disconnect() {
		if (running) {
			running = false;
			this.interrupt();
			try {
				s.close();
			} catch (IOException e) {}
			cs.disconnectHandler(this);
		}
	}

}
