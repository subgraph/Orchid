package com.subgraph.orchid;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.subgraph.orchid.data.HexDigest;
import com.subgraph.orchid.data.Timestamp;
import com.subgraph.orchid.directory.consensus.DirectorySignature;

public interface ConsensusDocument extends Document {
	
	Timestamp getValidAfterTime();
	Timestamp getFreshUntilTime();
	Timestamp getValidUntilTime();
	int getConsensusMethod();
	int getVoteSeconds();
	int getDistSeconds();
	Set<String> getClientVersions();
	Set<String> getServerVersions();
	boolean isLive();
	List<RouterStatus> getRouterStatusEntries();
	List<DirectorySignature> getDocumentSignatures();
	boolean canVerifySignatures(Map<HexDigest, KeyCertificate> certificates);
	boolean verifySignatures(Map<HexDigest, KeyCertificate> certificates);
	HexDigest getSigningHash();
	
	int getCircWindowParameter();
	int getWeightScaleParameter();
	
	int getBandwidthWeight(String tag);
}
