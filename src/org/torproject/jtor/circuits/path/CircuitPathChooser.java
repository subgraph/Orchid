package org.torproject.jtor.circuits.path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.torproject.jtor.circuits.guards.EntryGuards;
import org.torproject.jtor.circuits.path.CircuitNodeChooser.WeightRule;
import org.torproject.jtor.data.exitpolicy.ExitTarget;
import org.torproject.jtor.directory.Directory;
import org.torproject.jtor.directory.Router;

public class CircuitPathChooser {
	private final CircuitNodeChooser nodeChooser;
	private EntryGuards entryGuards;
	private boolean useEntryGuards;
	
	public CircuitPathChooser(Directory directory) {
		this.nodeChooser = new CircuitNodeChooser(directory);
		this.entryGuards = null;
		this.useEntryGuards = false;
	}

	public void enableEntryGuards(EntryGuards entryGuards) {
		this.entryGuards = entryGuards;
		this.useEntryGuards = true;
	}

	public List<Router> chooseDirectoryPath() {
		final Router dir = nodeChooser.chooseDirectory();
		return Arrays.asList(dir);
	}
	
	public List<Router> choosePathForTargets(List<ExitTarget> targets) throws InterruptedException {
		final Set<Router> excluded = new HashSet<Router>();
		final Router exitRouter = chooseExitNodeForTargets(targets);
		excluded.add(exitRouter);
		final Router middleRouter = chooseMiddleNode(excluded);
		excluded.add(middleRouter);
		final Router entryRouter = chooseEntryNode(excluded);
		return Arrays.asList(entryRouter, middleRouter, exitRouter);
	}

	Router chooseEntryNode(final Set<Router> excludedRouters) throws InterruptedException {
		if(useEntryGuards) {
			return entryGuards.chooseRandomGuard(excludedRouters);
		}

		if(!useEntryGuards) {
			throw new IllegalStateException();
		}
		return nodeChooser.chooseRandomNode(WeightRule.WEIGHT_FOR_GUARD, new RouterFilter() {
			public boolean filter(Router router) {
				return router.isPossibleGuard() && !excludedRouters.contains(router);
			}
		});
	}

	Router chooseMiddleNode(final Set<Router> excludedRouters) {
		return nodeChooser.chooseRandomNode(WeightRule.WEIGHT_FOR_MID, new RouterFilter() {
			public boolean filter(Router router) {
				return router.isFast() && !excludedRouters.contains(router);
			}
		});
	}

	Router chooseExitNodeForTargets(List<ExitTarget> targets) {
		final List<Router> routers = filterForExitTargets(
				getUsableExitRouters(), targets);
		return nodeChooser.chooseExitNode(routers);
	}
	
	private List<Router> getUsableExitRouters() {
		final List<Router> result = new ArrayList<Router>();
		for(Router r: nodeChooser.getUsableRouters(true)) {
			if(r.isExit() && !r.isBadExit()) {
				result.add(r);
			}
		}
		return result;
	}

	private List<Router> filterForExitTargets(List<Router> routers, List<ExitTarget> exitTargets) {
		int bestSupport = 0;
		if(exitTargets.isEmpty()) {
			return routers;
		}
		
		final int[] nSupport = new int[routers.size()];
		
		for(int i = 0; i < routers.size(); i++) {
			final Router r = routers.get(i);
			nSupport[i] = countTargetSupport(r, exitTargets);
			if(nSupport[i] > bestSupport) {
				bestSupport = nSupport[i];
			}
		}
		
		if(bestSupport == 0) {
			return routers;
		}

		final List<Router> results = new ArrayList<Router>();
		for(int i = 0; i < routers.size(); i++) {
			if(nSupport[i] == bestSupport) {
				results.add(routers.get(i));
			}
		}
		return results;
	}

	private int countTargetSupport(Router router, List<ExitTarget> targets) {
		int count = 0;
		for(ExitTarget t: targets) {
			if(routerSupportsTarget(router, t)) {
				count += 1;
			}
		}
		return count;
	}

	private boolean routerSupportsTarget(Router router, ExitTarget target) {
		if(target.isAddressTarget()) {
			return router.exitPolicyAccepts(target.getAddress(), target.getPort());
		} else {
			return router.exitPolicyAccepts(target.getPort());
		}
	}
}
