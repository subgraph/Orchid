package org.torproject.jtor.circuits;

import org.torproject.jtor.data.IPv4Address;

public interface Stream {
	boolean isExitStream();
	int getExitPort();
	IPv4Address getExitAddress();
	String getExitHost();

}
