package org.torproject.jtor.circuits.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class CircuitPredictor {

	private final static long TIMEOUT_MS = 60 * 60 * 1000; // One hour
	
	private final Map<Integer, Date> portsSeen;
	
	public CircuitPredictor() {
		portsSeen = new HashMap<Integer,Date>();
		portsSeen.put(80, new Date());
	}
	
	void addExitPortRequest(int port) {
		synchronized (portsSeen) {
			portsSeen.put(port, new Date());	
		}
	}
	
	
	private boolean isEntryExpired(Entry<Integer, Date> e, Date now) {
		return (now.getTime() - e.getValue().getTime()) > TIMEOUT_MS;
	}
	
	private void removeExpiredPorts() {
		final Date now = new Date();
		final Iterator<Entry<Integer, Date>> it = portsSeen.entrySet().iterator();
		while(it.hasNext()) {
			if(isEntryExpired(it.next(), now)) {
				it.remove();
			}
		}
	}
	
	Set<Integer> getPredictedPorts() {
		synchronized (portsSeen) {
			removeExpiredPorts();
			return new HashSet<Integer>(portsSeen.keySet());
		}
	}
	
	List<PredictedPortTarget> getPredictedPortTargets() {
		final List<PredictedPortTarget> targets = new ArrayList<PredictedPortTarget>();
		for(int p: getPredictedPorts()) {
			targets.add(new PredictedPortTarget(p));
		}
		return targets;
	}
}
