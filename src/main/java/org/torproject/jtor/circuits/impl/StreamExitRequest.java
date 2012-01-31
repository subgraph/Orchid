package org.torproject.jtor.circuits.impl;

import org.torproject.jtor.circuits.OpenStreamResponse;
import org.torproject.jtor.data.IPv4Address;
import org.torproject.jtor.data.exitpolicy.ExitTarget;

public class StreamExitRequest implements ExitTarget {

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
		this.hostname = "";
	}

	StreamExitRequest(CircuitManagerImpl circuitManager, String hostname, int port) {
		isAddress = false;
		this.circuitManager = circuitManager;
		this.address = null;
		this.hostname = hostname;
		this.port = port;
	}

	public boolean isAddressTarget() {
		return isAddress;
	}

	public IPv4Address getAddress() {
		return address;
	}

	public String getHostname() {
		return hostname;
	}

	public int getPort() {
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
	
	public boolean equals(Object ob) {
		if(this == ob) return true;
		if(!(ob instanceof StreamExitRequest))
			return false;
		StreamExitRequest other = (StreamExitRequest) ob;
		if(address != null && isAddress)
			return (other.isAddress && address.equals(other.address) && port == other.port);
		else 
			return (!other.isAddress && hostname.equals(other.hostname) && port == other.port); 
	}
	
	public int hashCode() {
		int hash = port;
		if(address != null) {
			hash *= 31;
			hash += address.hashCode();
		}
		if(hostname != null) {
			hash *= 31;
			hash += hostname.hashCode();
		}
		return hash;	
	}
}
