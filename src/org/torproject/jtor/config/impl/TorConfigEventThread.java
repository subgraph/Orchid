package org.torproject.jtor.config.impl;

import org.torproject.jtor.events.Event;

public class TorConfigEventThread extends Thread {
	
	private static final int sleeptime = 2000;

	private TorConfigImpl tc;
	private boolean running = false;

	public TorConfigEventThread(TorConfigImpl tc) {
		this.tc = tc;
		running = true;
		this.start();
	}

	public void run() {
		while (running) {
			if (tc.isConfigChanged()) {
				tc.setConfigChanged(false);
				tc.getConfigChangedManager().fireEvent(new Event() {});
			}
			try {
				Thread.yield();
				Thread.sleep(sleeptime);
			} catch (InterruptedException e) {}
		}
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

}
