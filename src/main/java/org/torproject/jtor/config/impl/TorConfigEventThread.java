package org.torproject.jtor.config.impl;

import org.torproject.jtor.events.Event;

public class TorConfigEventThread extends Thread {

	private static final int SLEEP_TIME = 2000;

	private TorConfigImpl tc;
	private boolean running = false;

	public TorConfigEventThread(TorConfigImpl tc) {
		this.tc = tc;
		this.running = true;
		start();
	}

	public void run() {
		while (running) {
			if (tc.isConfigChanged()) {
				tc.setConfigChanged(false);
				tc.getConfigChangedManager().fireEvent(new Event() {});
			}
			try {
				Thread.yield();
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e) {}
		}
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

}
