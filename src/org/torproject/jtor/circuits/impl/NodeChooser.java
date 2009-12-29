package org.torproject.jtor.circuits.impl;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.torproject.jtor.crypto.TorRandom;
import org.torproject.jtor.data.IPv4Address;
import org.torproject.jtor.directory.Directory;
import org.torproject.jtor.directory.Router;

public class NodeChooser {
	private final CircuitManagerImpl circuitManager;
	private final Directory directory;
	private final TorRandom random;

	NodeChooser(CircuitManagerImpl circuitManager, Directory directory) {
		this.circuitManager = circuitManager;
		this.directory = directory;
		this.random = new TorRandom();
	}

	Router chooseEntryNode(NodeChoiceConstraints ncc) {
		ncc.setNeedGuard(true);
		ncc.setWeightAsGuard(true);
		final List<Router> filteredRouters = filterForRouterList(
				filterForConstraintFlags(directory.getAllRouters(), ncc), 
				ncc.getExcludedRouters());
		return chooseRandomRouterByBandwidth(filteredRouters, ncc);
	}

	Router chooseMiddleNode(NodeChoiceConstraints ncc) {
		final List<Router> filteredRouters = filterForRouterList(directory.getAllRouters(), ncc.getExcludedRouters());
		return chooseRandomRouterByBandwidth(filteredRouters, ncc);
	}

	private List<Router> filterForRouterList(List<Router> routers, List<Router> excludedRouters) {
		final List<Router> resultRouters = new ArrayList<Router>();
		final Set<Router> excludedSet = routerListToSet(excludedRouters);

		for(Router r : routers) { 
			if(!excludedSet.contains(r))
				resultRouters.add(r);
		}

		return resultRouters;
	}

	private Set<Router> routerListToSet(List<Router> routers) {
		final Set<Router> routerSet = new HashSet<Router>();
		for(Router r : routers)
			routerSet.add(r);
		return routerSet;
	}

	Router chooseExitNodeForPort(int port, NodeChoiceConstraints ncc) {
		final List<StreamExitRequest> pendingExitStreams = circuitManager.getPendingExitStreams();
		final List<Router> allRouters = directory.getAllRouters();
		final List<Router> exitRouters = filterForExitDestination(allRouters, null, port);
		final List<Router> filteredPending = filterForPendingStreams(exitRouters, pendingExitStreams);

		return chooseRandomRouterByBandwidth(filteredPending, ncc);

	}

	private List<Router> filterForConstraintFlags(List<Router> routers, NodeChoiceConstraints ncc) {
		final List<Router> resultRouters = new ArrayList<Router>();
		for(Router r : routers) {
			if(testConstraintFlags(r, ncc)) resultRouters.add(r);							
		}
		return resultRouters;
	}

	private boolean testConstraintFlags(Router router, NodeChoiceConstraints ncc) {
		if(ncc.getNeedCapacity() && !router.isFast())
			return false;
		if(ncc.getNeedUptime() && !router.isStable())
			return false;
		if(ncc.getNeedGuard() && !router.isPossibleGuard())
			return false;

		return true;
	}

	private List<Router> filterForExitDestination(List<Router> routers, IPv4Address address, int port) {
		final List<Router> resultRouters = new ArrayList<Router>();
		for(Router r : routers) {
			if(r.isRunning() && r.isValid() && !(r.isHibernating() || r.isBadExit()) && 
					routerAcceptsDestination(r, address, port))
				resultRouters.add(r);
		}
		return resultRouters;
	}

	private List<Router> filterForPendingStreams(List<Router> routers, List<StreamExitRequest> pendingStreams) {
		int bestSupport = 0;
		if(pendingStreams.isEmpty())
			return routers;
		
		final int[] nSupport = new int[routers.size()];
		for(int i = 0; i < routers.size(); i++) {
			final Router r = routers.get(i);
			for(StreamExitRequest request: pendingStreams) {
				if(request.isAddressRequest()) {
					if(r.exitPolicyAccepts(request.getAddress(), request.getPort()))
						nSupport[i]++;
				} else {
					if(r.exitPolicyAccepts(request.getPort()))
						nSupport[i]++;
				}
			}
			if(nSupport[i] > bestSupport)
				bestSupport = nSupport[i];
		}
		if(bestSupport == 0)
			return routers;

		final List<Router> results = new ArrayList<Router>();
		for(int i = 0; i < routers.size(); i++) {
			if(nSupport[i] == bestSupport)
				results.add(routers.get(i));
		}
		return results;
	}

	private boolean routerAcceptsDestination(Router router, IPv4Address address, int port) {
		if(address == null)
			return router.exitPolicyAccepts(port);
		else
			return router.exitPolicyAccepts(address, port);
	}

	private Router chooseRandomRouterByBandwidth(List<Router> routers, NodeChoiceConstraints ncc) {
		//final boolean exitWeighted = true;
		//final boolean guardWeighted = false;
		final long[] bandwidths = new long[routers.size()];
		final BitSet exitBits = new BitSet(routers.size());
		final BitSet guardBits = new BitSet(routers.size());
		long totalNonexitBandwidth = 0;
		long totalExitBandwidth = 0;
		long totalBandwidth = 0;
		long totalNonguardBandwidth = 0;
		long totalGuardBandwidth = 0;
		int nUnknown = 0;
		for(int i = 0; i < routers.size(); i++) {
			final Router r = routers.get(i);
			final boolean isExit = r.isExit();
			final boolean isGuard = r.isPossibleGuard();
			final boolean isFast = r.isFast();
			boolean isKnown = true;
			int flags = 0;
			int thisBandwidth = 0;
			if(r.getEstimatedBandwidth() != 0) {
				thisBandwidth = kbToBytes(r.getEstimatedBandwidth());
			} else {
				isKnown = false;
				flags = (isFast) ? 1 : 0;
				flags |= isExit ? 2 : 0;
				flags |= isGuard ? 4 : 0;
			}
			if(isExit)
				exitBits.set(i);
			if(isGuard)
				guardBits.set(i);
			if(isKnown) {
				bandwidths[i] = thisBandwidth;
				if(isGuard)
					totalGuardBandwidth += thisBandwidth;
				else
					totalNonguardBandwidth += thisBandwidth;

				if(isExit)
					totalExitBandwidth += thisBandwidth;
				else
					totalNonexitBandwidth += thisBandwidth;
			} else {
				nUnknown++;
				bandwidths[i] = -flags;
			}
		}

		if(nUnknown > 0) {
			long avgFast, avgSlow;
			if(totalExitBandwidth + totalNonexitBandwidth > 0) {
				final int nKnown = routers.size() - nUnknown;
				avgFast = avgSlow = (long) ((totalExitBandwidth + totalNonexitBandwidth) / nKnown);
			} else {
				avgFast = 40000;
				avgSlow = 20000;
			}
			for(int i = 0; i < routers.size(); i++) {
				long bw = bandwidths[i];
				if(bw >= 0)
					continue;
				boolean isExit = ((-bw)&2) != 0;
				boolean isGuard = ((-bw) & 4) != 0;
				boolean isFast = ((-bw) & 1) != 0;
				bandwidths[i] = (isFast)? avgFast : avgSlow;
				if(isExit)
					totalExitBandwidth += bandwidths[i];
				else
					totalNonexitBandwidth += bandwidths[i];
				if(isGuard)
					totalGuardBandwidth += bandwidths[i];
				else
					totalNonguardBandwidth += bandwidths[i];
			}
		}

		if(totalExitBandwidth + totalNonexitBandwidth == 0) {
			return routers.get(random.nextInt(routers.size()));
		}

		double allBandwidth = (double) (totalExitBandwidth + totalNonexitBandwidth);
		double exitBandwidth = (double) (totalExitBandwidth);
		double guardBandwidth = (double) totalGuardBandwidth;

		double exitWeight;
		double guardWeight;
		if(ncc.getWeightAsExit())
			exitWeight = 1.0;
		else
			exitWeight = 1.0 - (allBandwidth / (3.0 * exitBandwidth));
		if(ncc.getWeightAsGuard())
			guardWeight = 1.0;
		else
			guardWeight = 1.0 - allBandwidth / (3.0 * guardBandwidth);

		if(exitWeight <= 0.0)
			exitWeight = 0.0;
		if(guardWeight <= 0.0)
			guardWeight = 0.0;
		totalBandwidth = 0;

		for(int i = 0; i < routers.size(); i++) {
			long bw;
			boolean isExit = exitBits.get(i);
			boolean isGuard = guardBits.get(i);
			if(isExit && isGuard)
				bw = (long) (bandwidths[i] * exitWeight * guardWeight);
			else if(isGuard)
				bw = (long) (bandwidths[i] * guardWeight);
			else if(isExit)
				bw = (long) (bandwidths[i] * exitWeight);
			else
				bw = bandwidths[i];
			totalBandwidth += bw;
		}
		// XXX should be 64 bit random
		long randBw = random.nextInt((int) totalBandwidth);
		long tmp = 0;
		for(int i = 0; i < routers.size(); i++) {
			boolean isExit = exitBits.get(i);
			boolean isGuard = guardBits.get(i);
			if(isExit && isGuard)
				tmp += (bandwidths[i] * exitWeight * guardWeight);
			else if(isGuard)
				tmp += (bandwidths[i] * guardWeight);
			else if(isExit)
				tmp += (bandwidths[i] * exitWeight);
			else
				tmp += bandwidths[i];
			if(tmp > randBw)
				return routers.get(i);
			
		}

		return routers.get(routers.size() - 1);
	}

	private int kbToBytes(int bw) {
		return (bw > (Integer.MAX_VALUE / 1000) ? Integer.MAX_VALUE : bw * 1000);
	}

}
