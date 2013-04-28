package org.torproject.jtor.circuits.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.torproject.jtor.Router;
import org.torproject.jtor.TorException;
import org.torproject.jtor.crypto.TorRandom;

public class CircuitStatus {

	enum CircuitState {
		UNCONNECTED("Unconnected"),
		BUILDING("Building"),
		FAILED("Failed"),
		OPEN("Open"),
		DESTROYED("Destroyed");
		String name;
		CircuitState(String name) { this.name = name; }
		public String toString() { return name; }
	}

	private Date timestampCreated;
	private Date timestampDirty;
	private int currentStreamId;
	private Object streamIdLock = new Object();
	private CircuitState state = CircuitState.UNCONNECTED;
	private List<Router> circuitPath = Collections.emptyList();

	CircuitStatus() {
		initializeCurrentStreamId();
	}

	private void initializeCurrentStreamId() {
		final TorRandom random = new TorRandom();
		currentStreamId = random.nextInt(0xFFFF) + 1;
	}

	synchronized void updateCreatedTimestamp() {
		timestampCreated = new Date();
		timestampDirty = null;
	}

	synchronized void updateDirtyTimestamp() {
		if(timestampDirty == null) {
			timestampDirty = new Date();
		}
	}

	synchronized long getMillisecondsElapsedSinceCreated() {
		return millisecondsElapsedSince(timestampCreated);
	}

	synchronized long getMillisecondsDirty() {
		return millisecondsElapsedSince(timestampDirty);
	}

	private static long millisecondsElapsedSince(Date then) {
		if(then == null)
			return 0;
		final Date now = new Date();
		return now.getTime() - then.getTime();
	}

	synchronized boolean isDirty() {
		return timestampDirty != null;
	}

	void setStateBuilding(List<Router> circuitPath) {
		state = CircuitState.BUILDING;
		this.circuitPath = Collections.unmodifiableList(circuitPath);
	}

	Router getFinalRouter() {
		if(state == CircuitState.UNCONNECTED)
			throw new TorException("Cannot retrieve last router from UNCONNECTED circuit");
		if(circuitPath.size() == 0) 
			throw new TorException("No routers on circuit (?!)");
		return circuitPath.get(circuitPath.size() - 1);
	}

	List<Router> getCircuitPath() {
		return circuitPath;
	}

	void setStateFailed() {
		state = CircuitState.FAILED;
	}

	void setStateOpen() {
		state = CircuitState.OPEN;
	}

	void setStateDestroyed() {
		state = CircuitState.DESTROYED;
	}

	boolean isBuilding() {
		return state == CircuitState.BUILDING;
	}

	boolean isConnected() {
		return state == CircuitState.OPEN;
	}

	boolean isUnconnected() {
		return state == CircuitState.UNCONNECTED;
	}

	String getStateAsString() {
		if(state == CircuitState.OPEN) {
			return state.toString() + " ["+ getDirtyString() + "]";
		}
		return state.toString();
	}

	private String getDirtyString() {
		if(!isDirty()) {
			return "Clean";
		} else {
			return "Dirty "+ (getMillisecondsDirty() / 1000) +"s"; 
		}
	}
	int nextStreamId() {
		synchronized(streamIdLock) {
			currentStreamId++;
			if(currentStreamId > 0xFFFF)
				currentStreamId = 1;
			return currentStreamId;
		}
	}

}
