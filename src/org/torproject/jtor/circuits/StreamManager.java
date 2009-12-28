package org.torproject.jtor.circuits;

import org.torproject.jtor.data.IPv4Address;

public interface StreamManager {
	OpenStreamResponse openExitStreamTo(String hostname, int port) throws InterruptedException;
	OpenStreamResponse openExitStreamTo(IPv4Address address, int port) throws InterruptedException;

}
