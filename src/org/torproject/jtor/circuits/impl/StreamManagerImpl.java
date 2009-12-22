package org.torproject.jtor.circuits.impl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.torproject.jtor.circuits.Stream;
import org.torproject.jtor.circuits.StreamManager;
import org.torproject.jtor.data.IPv4Address;

public class StreamManagerImpl implements StreamManager {
	
	private final List<StreamExitRequest> pendingExitStreams = new LinkedList<StreamExitRequest>();
	
	List<StreamExitRequest> getPendingExitStreams() {
		synchronized(pendingExitStreams) {
			return new ArrayList<StreamExitRequest>(pendingExitStreams);
		}
	}
	
	public Stream openExitStreamTo(String hostname, int port) throws InterruptedException {
		return openExitStreamByRequest(new StreamExitRequest(hostname, port));
	}
	
	public Stream openExitStreamTo(IPv4Address address, int port) throws InterruptedException {
		return openExitStreamByRequest(new StreamExitRequest(address, port));
	}
	
	void streamIsConnected(StreamExitRequest request) {
		synchronized(pendingExitStreams) {
			pendingExitStreams.remove(request);
			pendingExitStreams.notifyAll();
		}
	}
	
	private Stream openExitStreamByRequest(StreamExitRequest request) throws InterruptedException {
		synchronized(pendingExitStreams) {
			pendingExitStreams.add(request);
			while(!request.isConnected())
				pendingExitStreams.wait();
		}
		return request.getAllocatedStream();
	}
}
