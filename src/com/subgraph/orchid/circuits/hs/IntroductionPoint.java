package com.subgraph.orchid.circuits.hs;

import com.subgraph.orchid.crypto.TorPublicKey;
import com.subgraph.orchid.data.HexDigest;
import com.subgraph.orchid.data.IPv4Address;

public class IntroductionPoint {

	private HexDigest identity;
	private IPv4Address address;
	private int onionPort;
	private TorPublicKey onionKey;
	private TorPublicKey serviceKey;
	
	IntroductionPoint(HexDigest identity) {
		this.identity = identity;
	}

	void setAddress(IPv4Address address) {
		this.address = address;
	}
	
	void setOnionPort(int onionPort) {
		this.onionPort = onionPort;
	}
	
	void setOnionKey(TorPublicKey onionKey) {
		this.onionKey = onionKey;
	}
	
	void setServiceKey(TorPublicKey serviceKey) {
		this.serviceKey = serviceKey;
	}
}
