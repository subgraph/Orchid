package org.torproject.jtor.directory;

import org.torproject.jtor.data.HexDigest;
import org.torproject.jtor.data.IPv4Address;

public interface VoteAuthorityEntry {
	String getNickname();
	HexDigest getIdentity();
	String getHostname();
	IPv4Address getAddress();
	int getDirectoryPort();
	int getRouterPort();
	String getContact();
	HexDigest getVoteDigest();

}
