package org.torproject.jtor.directory;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.torproject.jtor.data.HexDigest;
import org.torproject.jtor.data.Timestamp;
import org.torproject.jtor.directory.impl.consensus.DirectorySignature;

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
}
