package org.torproject.jtor.circuits;

import org.torproject.jtor.data.IPv4Address;

public interface StreamManager {
	Stream openExitStreamTo(String hostname, int port) throws InterruptedException;
	Stream openExitStreamTo(IPv4Address address, int port) throws InterruptedException;

}
