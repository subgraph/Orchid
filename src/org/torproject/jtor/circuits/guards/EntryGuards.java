package org.torproject.jtor.circuits.guards;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.torproject.jtor.circuits.path.CircuitNodeChooser;
import org.torproject.jtor.circuits.path.CircuitNodeChooser.WeightRule;
import org.torproject.jtor.circuits.path.RouterFilter;
import org.torproject.jtor.connections.ConnectionCache;
import org.torproject.jtor.crypto.TorRandom;
import org.torproject.jtor.data.HexDigest;
import org.torproject.jtor.directory.Directory;
import org.torproject.jtor.directory.GuardEntry;
import org.torproject.jtor.directory.Router;

public class EntryGuards {
	private final static Logger logger = Logger.getLogger(EntryGuards.class.getName());
	
	private final static int MIN_USABLE_GUARDS = 2;
	private final static int NUM_ENTRY_GUARDS = 3;
	
	private final TorRandom random;
	private final CircuitNodeChooser nodeChooser;
	private final ConnectionCache connectionCache;
	private final Directory directory;
	private final Set<Router> pendingInitialProbes;
	private final Set<GuardEntry> pendingRetestProbes;
	private final Executor executor;
	
	public EntryGuards(ConnectionCache connectionCache, Directory directory) {
		this.random = new TorRandom();
		this.nodeChooser = new CircuitNodeChooser(directory);
		this.connectionCache = connectionCache;
		this.directory = directory;
		this.pendingInitialProbes = new HashSet<Router>();
		this.pendingRetestProbes = new HashSet<GuardEntry>();
		this.executor = Executors.newCachedThreadPool();
	}
	
	public Router chooseRandomGuard(Set<Router> excluded) throws InterruptedException {
		List<Router> usableGuards;
		synchronized(pendingInitialProbes) {
			usableGuards = getUsableGuards(excluded);
			while(usableGuards.size() < MIN_USABLE_GUARDS) {
				maybeChooseNew(usableGuards.size(), getExcludedForNew(excluded, usableGuards));
				pendingInitialProbes.wait();
				usableGuards = getUsableGuards(excluded);
			}
		}
		final int n = Math.min(usableGuards.size(), NUM_ENTRY_GUARDS);
		return usableGuards.get(random.nextInt(n));
	}
	
	private Set<Router> getExcludedForNew(Set<Router> excluded, List<Router> usable) {
		final Set<Router> set = new HashSet<Router>();
		set.addAll(excluded);
		set.addAll(usable);
		set.addAll(pendingInitialProbes);
		return set;
	}

	private void maybeChooseNew(int usableSize, Set<Router> excluded) {
		int sz = usableSize + pendingInitialProbes.size();
		while(sz < MIN_USABLE_GUARDS) {
			
			Router newGuard = chooseNewGuard(excluded);
			if(newGuard == null) {
				return;
			}
			logger.fine("Testing "+ newGuard + " as a new guard since we only have "+ usableSize + " usable guards");
			initialProbeOfRouter(newGuard);
			sz += 1;
		}
	}

	private Router chooseNewGuard(final Set<Router> excluded) {
		return nodeChooser.chooseRandomNode(WeightRule.WEIGHT_FOR_GUARD, new RouterFilter() {
			public boolean filter(Router router) {
				return isRouterUsable(router) && !excluded.contains(router);
			}
		});
	}
	
	private void removeEntryFromPending(GuardEntry entry) {
		synchronized (pendingRetestProbes) {
			pendingRetestProbes.remove(entry);
		}
	}

	void probeConnectionFailed(Router router, GuardEntry entry, boolean isInitialProbe) {
		if(isInitialProbe) {
			synchronized (pendingInitialProbes) {
				pendingInitialProbes.remove(router);
				pendingInitialProbes.notifyAll();
			}
		} else {
			removeEntryFromPending(entry);
			entry.setLastConnectAttempt(new Date());
		}
	}

	void probeConnectionSucceeded(Router router, GuardEntry entry, boolean isInitialProbe) {
		if(isInitialProbe) {
			synchronized (pendingInitialProbes) {
				logger.fine("Probe connection to "+ router + " succeeded.  Adding it as a new entry guard.");
				pendingInitialProbes.remove(router);
				directory.addGuardEntry(entry);
				pendingInitialProbes.notifyAll();
			}
			retestUnreachable();
		} else {
			removeEntryFromPending(entry);
			entry.clearDownSince();
		}
	}

	private void retestUnreachable() {
		for(GuardEntry e: directory.getGuardEntries()) {
			if(e.getDownSince() != null) {
				retestProbeOfEntry(e);
			}
		}
	}

	private List<Router> getUsableGuards(Set<Router> excluded) {
		List<Router> usableRouters = new ArrayList<Router>();
		for(GuardEntry entry: directory.getGuardEntries()) {
			Router router = getRouterForEntry(entry);
			if(router == null || !isRouterUsable(router)) {
				markNotUsable(entry);
			} else {
				if(!excluded.contains(router)) {
					usableRouters.add(router);
				}
				markUsable(entry);
			}
		}
		return usableRouters;
	}
	
	private void markNotUsable(GuardEntry entry) {
		if(entry.getUnlistedSince() == null) {
			entry.setUnlistedSince(new Date());
		}
	}
	
	private void markUsable(GuardEntry entry) {
		if(entry.getUnlistedSince() != null) {
			entry.clearUnlistedSince();
		}
	}

	private Router getRouterForEntry(GuardEntry entry) {
		final HexDigest identity = HexDigest.createFromString(entry.getIdentity());
		return directory.getRouterByIdentity(identity);
	}
	
	private boolean isRouterUsable(Router router) {
		return router.isPossibleGuard() && router.isRunning();
	}
	
	private void initialProbeOfRouter(Router router) {
		synchronized (pendingInitialProbes) {
			if(pendingInitialProbes.contains(router)) {
				return;
			} else {
				pendingInitialProbes.add(router);
			}
		}
		final GuardEntry entry = directory.createGuardEntryFor(router);
		executor.execute(new GuardProbeTask(connectionCache, this, router, entry, true));
	}
	
	private void retestProbeOfEntry(GuardEntry entry) {
		final Router router;
		synchronized (pendingRetestProbes) {
			router = getRouterForEntry(entry);
			if(router == null || !isRouterUsable(router)) {
				return;
			}
			if(pendingRetestProbes.contains(entry)) {
				return;
			} else {
				pendingRetestProbes.add(entry);
			}
		}
		executor.execute(new GuardProbeTask(connectionCache, this, router, entry, false));
	}
}
