package org.torproject.jtor.directory;

import org.torproject.jtor.data.HexDigest;
import org.torproject.jtor.data.IPv4Address;
import org.torproject.jtor.data.Timestamp;
import org.torproject.jtor.data.exitpolicy.ExitPorts;

public interface RouterStatus {
	String getNickname();
	HexDigest getIdentity();
	HexDigest getDescriptorDigest();
	Timestamp getPublicationTime();
	IPv4Address getAddress();
	int getRouterPort();
	boolean isDirectory();
	int getDirectoryPort();
	boolean hasFlag(String flag);
	String getVersion();
	int getEstimatedBandwidth();
	int getMeasuredBandwidth();
	ExitPorts getExitPorts();
}
