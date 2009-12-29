package org.torproject.jtor.circuits.impl;

import org.torproject.jtor.circuits.OpenStreamResponse;
import org.torproject.jtor.data.IPv4Address;

public class StreamExitRequest {

	private final boolean isAddress;
	private final IPv4Address address;
	private final String hostname;
	private final int port;
	private final CircuitManagerImpl circuitManager;
	private OpenStreamResponse response;
	private boolean isReserved;

	StreamExitRequest(CircuitManagerImpl circuitManager, IPv4Address address, int port) {
		this.circuitManager = circuitManager;
		isAddress = true;
		this.address = address;
		this.port = port;
		this.hostname = null;
	}

	StreamExitRequest(CircuitManagerImpl circuitManager, String hostname, int port) {
		isAddress = false;
		this.circuitManager = circuitManager;
		this.address = null;
		this.hostname = hostname;
		this.port = port;
	}

	boolean isAddressRequest() {
		return isAddress;
	}

	IPv4Address getAddress() {
		return address;
	}

	String getHostname() {
		return hostname;
	}

	int getPort() {
		return port;
	}

	void setCompleted(OpenStreamResponse response) {
		this.response = response;
		circuitManager.streamRequestIsCompleted(this);
	}
	
	OpenStreamResponse getResponse() {
		return response;
	}

	boolean isCompleted() {
		return response != null;
	}
	
	synchronized boolean reserveRequest() {
		if(isReserved) return false;
		isReserved = true;
		return true;
	}
	
	boolean isReserved() {
		return isReserved;
	}
	
	synchronized void unreserveRequest() {
		isReserved = false;
	}
	
	public String toString() {
		if(isAddress)
			return address + ":"+ port;
		else
			return hostname + ":"+ port;
	}

}
