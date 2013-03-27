package org.torproject.jtor.circuits.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.Set;

import org.torproject.jtor.circuits.Circuit;
import org.torproject.jtor.circuits.CircuitBuildHandler;
import org.torproject.jtor.directory.Router;

public class CircuitPredictor {
	private final static Logger logger = Logger.getLogger(CircuitPredictor.class.getName());

	private final static long TIMEOUT_MS = 60 * 60 * 1000; // One hour
	private final static int MAXIMUM_CLEAN_CIRCUIT_COUNT = 12;
	
	private final Map<Integer, Date> portsSeen;
	private final CircuitPathChooser pathChooser;
	private final CircuitManagerImpl circuitManager;
	
	public CircuitPredictor(CircuitPathChooser pathChooser, CircuitManagerImpl circuitManager) {
		this.pathChooser = pathChooser; 
		this.circuitManager = circuitManager;
		portsSeen = new HashMap<Integer,Date>();
		portsSeen.put(80, new Date());
	}
	
	void addExitPortRequest(int port) {
		synchronized (portsSeen) {
			portsSeen.put(port, new Date());	
		}
	}
	
	
	List<CircuitCreationRequest> generatePredictedCircuitRequests(Set<Circuit> cleanExitCircuits, CircuitBuildHandler buildHandler) {
		if(cleanExitCircuits.size() >= MAXIMUM_CLEAN_CIRCUIT_COUNT) {
			return Collections.emptyList();
		}
		
		final Set<Integer> portsToCover = new HashSet<Integer>();
		
		for(Integer port : getPredictedPorts()) {
			if(countCircuitsSupportingPort(port, cleanExitCircuits) < 2) {
				portsToCover.add(port);
			}
		}
		if(portsToCover.isEmpty()) {
			return Collections.emptyList();
		}
		return createRequestsForPorts(portsToCover, buildHandler);
	}
	
	private int countCircuitsSupportingPort(int port, Set<Circuit> circuits) {
		int count = 0;
		for(Circuit c: circuits) {
			if(c.canHandleExitToPort(port)) {
				count += 1;
			}
		}
		return count;
	}

	private List<CircuitCreationRequest> createRequestsForPorts(Set<Integer> portsToCover, CircuitBuildHandler buildHandler) {
		List<CircuitCreationRequest> requests = new ArrayList<CircuitCreationRequest>();
		logger.fine("Creating requests to cover predicted ports: "+ portsToCover);
		while(!portsToCover.isEmpty()) {
			final List<Router> path = pathChooser.choosePathForPredictedPorts(portsToCover);
			removePortsHandledByExit(portsToCover, path.get(path.size() - 1));
			requests.add(new CircuitCreationRequest(CircuitImpl.create(circuitManager), path, buildHandler, false));
		}
		logger.fine("Created "+ requests.size() + " requests ");
		return requests;
	}

	private void removePortsHandledByExit(Set<Integer> portsToCover, Router exit) {
		final Iterator<Integer> it = portsToCover.iterator();
		while(it.hasNext()) {
			if(exit.exitPolicyAccepts(it.next())) {
				it.remove();
			} 
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
	
	private Set<Integer> getPredictedPorts() {
		synchronized (portsSeen) {
			removeExpiredPorts();
			return new HashSet<Integer>(portsSeen.keySet());
		}
	}
}
