package com.subgraph.orchid.circuits;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.subgraph.orchid.dashboard.DashboardRenderable;
import com.subgraph.orchid.dashboard.DashboardRenderer;

public class CircuitPredictor implements DashboardRenderable {

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
	
	Date getLastSeen(int port) {
		return portsSeen.get(port);
	}

	List<PredictedPortTarget> getPredictedPortTargets() {
		final List<PredictedPortTarget> targets = new ArrayList<PredictedPortTarget>();
		for(int p: getPredictedPorts()) {
			targets.add(new PredictedPortTarget(p));
		}
		return targets;
	}

	public void dashboardRender(DashboardRenderer renderer, PrintWriter writer, int flags)
			throws IOException {
		
		if((flags & DASHBOARD_PREDICTED_PORTS) == 0) {
			return;
		}
		writer.println("[Predicted Ports] ");
		for(int port : portsSeen.keySet()) {
			writer.write(" "+ port);
			Date lastSeen = portsSeen.get(port);
			if(lastSeen != null) {
				Date now = new Date();
				long ms = now.getTime() - lastSeen.getTime();
				writer.write(" (last seen "+ TimeUnit.MINUTES.convert(ms, TimeUnit.MILLISECONDS) +" minutes ago)");
			}
			writer.println();
		}
		writer.println();
	}
}
