package org.torproject.jtor.circuits.impl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.torproject.jtor.circuits.OpenStreamResponse;
import org.torproject.jtor.circuits.StreamManager;
import org.torproject.jtor.data.IPv4Address;

public class StreamManagerImpl implements StreamManager {
	
	private final List<StreamExitRequest> pendingExitStreams = new LinkedList<StreamExitRequest>();
	
	List<StreamExitRequest> getPendingExitStreams() {
		synchronized(pendingExitStreams) {
			return new ArrayList<StreamExitRequest>(pendingExitStreams);
		}
	}
	
	public OpenStreamResponse openExitStreamTo(String hostname, int port) throws InterruptedException {
		return openExitStreamByRequest(new StreamExitRequest(hostname, port));
	}
	
	public OpenStreamResponse openExitStreamTo(IPv4Address address, int port) throws InterruptedException {
		return openExitStreamByRequest(new StreamExitRequest(address, port));
	}
	
	void streamIsConnected(StreamExitRequest request) {
		synchronized(pendingExitStreams) {
			pendingExitStreams.remove(request);
			pendingExitStreams.notifyAll();
		}
	}
	
	private OpenStreamResponse openExitStreamByRequest(StreamExitRequest request) throws InterruptedException {
		synchronized(pendingExitStreams) {
			pendingExitStreams.add(request);
			while(!request.isCompleted())
				pendingExitStreams.wait();
		}
		return request.getResponse();
	}
}
