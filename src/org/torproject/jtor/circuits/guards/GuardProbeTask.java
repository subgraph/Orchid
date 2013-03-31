package org.torproject.jtor.circuits.guards;

import org.torproject.jtor.circuits.ConnectionIOException;
import org.torproject.jtor.connections.ConnectionCache;
import org.torproject.jtor.directory.GuardEntry;
import org.torproject.jtor.directory.Router;

public class GuardProbeTask implements Runnable{

	private final ConnectionCache connectionCache;
	private final EntryGuards entryGuards;
	private final Router router;
	private final GuardEntry entry;
	private boolean isInitialProbe;
	
	public GuardProbeTask(ConnectionCache connectionCache, EntryGuards entryGuards, Router router, GuardEntry entry, boolean isInitialProbe) {
		this.connectionCache = connectionCache;
		this.entryGuards = entryGuards;
		this.router = router;
		this.entry = entry;
		this.isInitialProbe = isInitialProbe;
	}
	
	public void run() {
		try {
			connectionCache.getConnectionTo(router, false);
			entryGuards.probeConnectionSucceeded(router, entry, isInitialProbe);
			return;
		} catch (ConnectionIOException e) {
			entryGuards.probeConnectionFailed(router, entry, isInitialProbe);
			return;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
