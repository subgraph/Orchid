package com.subgraph.orchid;

import java.util.Set;

import com.subgraph.orchid.crypto.TorPublicKey;
import com.subgraph.orchid.data.HexDigest;
import com.subgraph.orchid.data.IPv4Address;

public interface RouterMicrodescriptor extends Document {
	enum CacheLocation { NOT_CACHED, CACHED_CACHEFILE, CACHED_JOURNAL }
	TorPublicKey getOnionKey();
	byte[] getNTorOnionKey();
	IPv4Address getAddress();
	int getRouterPort();
	Set<String> getFamilyMembers();
	boolean exitPolicyAccepts(IPv4Address address, int port);
	boolean exitPolicyAccepts(int port);
	HexDigest getDescriptorDigest();
	long getLastListed();
	void setCacheLocation(CacheLocation location);
	CacheLocation getCacheLocation();
	int getBodyLength();
}
