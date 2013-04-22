package org.torproject.jtor.circuits.impl;

import java.util.concurrent.TimeoutException;

import org.torproject.jtor.circuits.OpenFailedException;
import org.torproject.jtor.circuits.Stream;
import org.torproject.jtor.circuits.StreamConnectFailedException;
import org.torproject.jtor.data.IPv4Address;
import org.torproject.jtor.data.exitpolicy.ExitTarget;
import org.torproject.jtor.misc.GuardedBy;

public class StreamExitRequest implements ExitTarget {
	
	private enum CompletionStatus {NOT_COMPLETED, SUCCESS, TIMEOUT, STREAM_OPEN_FAILURE, EXIT_FAILURE, INTERRUPTED};
	
	private final boolean isAddress;
	private final IPv4Address address;
	private final String hostname;
	private final int port;
	private final CircuitManagerImpl circuitManager;
	
	@GuardedBy("this") private Stream stream;
	@GuardedBy("this") private boolean isReserved;
	@GuardedBy("this") private CompletionStatus completionStatus;
	@GuardedBy("this") private int streamOpenFailReason;
	@GuardedBy("this") private int retryCount;
	@GuardedBy("this") private long specificTimeout;

	StreamExitRequest(CircuitManagerImpl circuitManager, IPv4Address address, int port) {
		this(circuitManager, true, "", address, port);
	}

	StreamExitRequest(CircuitManagerImpl circuitManager, String hostname, int port) {
		this(circuitManager, false, hostname, null, port);
	}
	
	private StreamExitRequest(CircuitManagerImpl circuitManager, boolean isAddress, String hostname, IPv4Address address, int port) {
		this.circuitManager = circuitManager;
		this.isAddress = isAddress;
		this.hostname = hostname;
		this.address = address;
		this.port = port;
		this.completionStatus = CompletionStatus.NOT_COMPLETED;
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

	public synchronized void setStreamTimeout(long timeout) {
		specificTimeout = timeout;
	}
	
	public synchronized long getStreamTimeout() {
		if(specificTimeout > 0) {
			return specificTimeout;
		} else if(retryCount < 2) {
			return 10 * 1000;
		} else {
			return 15 * 1000;
		}
	}

	synchronized void setCompletedTimeout() {
		newStatus(CompletionStatus.TIMEOUT);
	}
	
	synchronized void setExitFailed() {
		newStatus(CompletionStatus.EXIT_FAILURE);
	}
	
	synchronized void setStreamOpenFailure(int reason) {
		streamOpenFailReason = reason;
		newStatus(CompletionStatus.STREAM_OPEN_FAILURE);
	}
	
	synchronized void setCompletedSuccessfully(Stream stream) {
		this.stream = stream;
		newStatus(CompletionStatus.SUCCESS);
	}
	
	synchronized void setInterrupted() {
		newStatus(CompletionStatus.INTERRUPTED);
	}

	private void newStatus(CompletionStatus newStatus) {
		if(completionStatus != CompletionStatus.NOT_COMPLETED) {
			throw new IllegalStateException("Attempt to set completion state to " + newStatus +" while status is "+ completionStatus);
		}
		completionStatus = newStatus;
		circuitManager.streamRequestIsCompleted(this);
	}
	
	synchronized Stream getStream() throws OpenFailedException, TimeoutException, StreamConnectFailedException, InterruptedException {
		switch(completionStatus) {
		case NOT_COMPLETED:
			throw new IllegalStateException("Request not completed");
		case EXIT_FAILURE:
			throw new OpenFailedException();
		case TIMEOUT:
			throw new TimeoutException();
		case STREAM_OPEN_FAILURE:
			throw new StreamConnectFailedException(streamOpenFailReason);
		case INTERRUPTED:
			throw new InterruptedException();
		case SUCCESS:
			return stream;
		default:
			throw new IllegalStateException("Unknown completion status");
		}
	}

	synchronized void resetForRetry() {
		retryCount += 1;
		streamOpenFailReason = 0;
		completionStatus = CompletionStatus.NOT_COMPLETED;
	}

	synchronized boolean isCompleted() {
		return completionStatus != CompletionStatus.NOT_COMPLETED;
	}
	
	synchronized boolean reserveRequest() {
		if(isReserved) return false;
		isReserved = true;
		return true;
	}
	
	synchronized boolean isReserved() {
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
