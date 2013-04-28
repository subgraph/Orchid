package com.subgraph.orchid;

import com.subgraph.orchid.data.HexDigest;
import com.subgraph.orchid.data.IPv4Address;

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
