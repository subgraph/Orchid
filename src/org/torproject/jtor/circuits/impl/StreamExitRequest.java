package org.torproject.jtor.circuits.impl;

import org.torproject.jtor.circuits.Stream;
import org.torproject.jtor.data.IPv4Address;

public class StreamExitRequest {

	private final boolean isAddress;
	private final IPv4Address address;
	private final String hostname;
	private final int port;
	private Stream allocatedStream;
	private boolean isReserved;

	StreamExitRequest(IPv4Address address, int port) {
		isAddress = true;
		this.address = address;
		this.port = port;
		this.hostname = null;
	}

	StreamExitRequest(String hostname, int port) {
		isAddress = false;
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

	void setAllocatedStream(Stream stream) {
		allocatedStream = stream;
	}

	Stream getAllocatedStream() {
		return allocatedStream;
	}

	boolean isConnected() {
		return allocatedStream != null;
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
