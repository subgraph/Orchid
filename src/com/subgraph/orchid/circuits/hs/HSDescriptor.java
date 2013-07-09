package com.subgraph.orchid.circuits.hs;

import java.util.ArrayList;
import java.util.List;

import com.subgraph.orchid.crypto.TorPublicKey;
import com.subgraph.orchid.data.HexDigest;
import com.subgraph.orchid.data.Timestamp;

public class HSDescriptor {
	private HexDigest descriptorId;
	private Timestamp publicationTime;
	private HexDigest secretIdPart;
	private TorPublicKey permanentKey;
	private int[] protocolVersions;
	private List<IntroductionPoint> introductionPoints;
	
	public HSDescriptor() {
		introductionPoints = new ArrayList<IntroductionPoint>();
	}
	
	void setPublicationTime(Timestamp ts) {
		this.publicationTime = ts;
	}
	
	void setSecretIdPart(HexDigest secretIdPart) {
		this.secretIdPart = secretIdPart;
	}
	
	void setDescriptorId(HexDigest descriptorId) {
		this.descriptorId = descriptorId;
	}
	
	void setPermanentKey(TorPublicKey permanentKey) {
		this.permanentKey = permanentKey;
	}
	
	void setProtocolVersions(int[] protocolVersions) {
		this.protocolVersions = protocolVersions;
	}

	void addIntroductionPoint(IntroductionPoint ip) {
		introductionPoints.add(ip);
	}

	HexDigest getDescriptorId() {
		return descriptorId;
	}
	
	int getVersion() {
		return 2;
	}
	
	TorPublicKey getPermanentKey() {
		return permanentKey;
	}
	
	HexDigest getSecretIdPart() {
		return secretIdPart;
	}
	
	Timestamp getPublicationTime() {
		return publicationTime;
	}
	
	int[] getProtocolVersions() {
		return protocolVersions;
	}
	

}
