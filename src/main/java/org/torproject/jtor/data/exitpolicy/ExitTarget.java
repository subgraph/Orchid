package org.torproject.jtor.data.exitpolicy;

import org.torproject.jtor.data.IPv4Address;

public interface ExitTarget {
	boolean isAddressTarget();
	IPv4Address getAddress();
	String getHostname();
	int getPort();
}
