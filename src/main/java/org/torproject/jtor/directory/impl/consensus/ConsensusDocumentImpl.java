package org.torproject.jtor.directory.impl.consensus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.torproject.jtor.crypto.TorPublicKey;
import org.torproject.jtor.data.HexDigest;
import org.torproject.jtor.data.Timestamp;
import org.torproject.jtor.directory.KeyCertificate;
import org.torproject.jtor.directory.RouterStatus;
import org.torproject.jtor.directory.ConsensusDocument;
import org.torproject.jtor.directory.VoteAuthorityEntry;

public class ConsensusDocumentImpl implements ConsensusDocument {

	private int consensusMethod;
	private Timestamp validAfter;
	private Timestamp freshUntil;
	private Timestamp validUntil;
	private int distDelaySeconds;
	private int voteDelaySeconds;
	private Set<String> clientVersions;
	private Set<String> serverVersions;
	private Set<String> knownFlags;

	private HexDigest signingHash;
	private Map<HexDigest, VoteAuthorityEntry> voteAuthorityEntries;
	private List<RouterStatus> routerStatusEntries;
	private List<DirectorySignature> signatures;
	private String rawDocumentData;

	void setConsensusMethod(int method) { consensusMethod = method; }
	void setValidAfter(Timestamp ts) { validAfter = ts; }
	void setFreshUntil(Timestamp ts) { freshUntil = ts; }
	void setValidUntil(Timestamp ts) { validUntil = ts; }
	void setDistDelaySeconds(int seconds) { distDelaySeconds = seconds; }
	void setVoteDelaySeconds(int seconds) { voteDelaySeconds = seconds; }
	void addClientVersion(String version) { clientVersions.add(version); }
	void addServerVersion(String version) { serverVersions.add(version); }
	void addSignature(DirectorySignature signature) { signatures.add(signature); }
	void setSigningHash(HexDigest hash) { signingHash = hash; }
	void setRawDocumentData(String rawData) { rawDocumentData = rawData; }

	ConsensusDocumentImpl() {
		clientVersions = new HashSet<String>();
		serverVersions = new HashSet<String>();
		knownFlags = new HashSet<String>();
		voteAuthorityEntries = new HashMap<HexDigest, VoteAuthorityEntry>();
		routerStatusEntries = new ArrayList<RouterStatus>();
		signatures = new ArrayList<DirectorySignature>();
	}

	void addKnownFlag(String flag) {
		knownFlags.add(flag);
	}

	void addVoteAuthorityEntry(VoteAuthorityEntry entry) {
		voteAuthorityEntries.put(entry.getVoteDigest(), entry);
	}

	void addRouterStatusEntry(RouterStatusImpl entry) {
		routerStatusEntries.add(entry);
	}

	public Timestamp getValidAfterTime() {
		return validAfter;
	}

	public Timestamp getFreshUntilTime() {
		return freshUntil;
	}

	public Timestamp getValidUntilTime() {
		return validUntil;
	}

	public int getConsensusMethod() {
		return consensusMethod;
	}

	public int getVoteSeconds() {
		return voteDelaySeconds;
	}

	public int getDistSeconds() {
		return distDelaySeconds;
	}

	public Set<String> getClientVersions() {
		return clientVersions;
	}

	public Set<String> getServerVersions() {
		return serverVersions;
	}

	public boolean isLive() {
		return !validUntil.hasPassed();
	}

	public List<RouterStatus> getRouterStatusEntries() {
		return Collections.unmodifiableList(routerStatusEntries);
	}

	public String getRawDocumentData() {
		return rawDocumentData;
	}

	public boolean isValidDocument() {
		return (validAfter != null) && (freshUntil != null)
				&& (validUntil != null) && (voteDelaySeconds > 0)
				&& (distDelaySeconds > 0) && (signingHash != null)
				&& !signatures.isEmpty();
	}

	public HexDigest getSigningHash() {
		return signingHash;
	}

	public List<DirectorySignature> getDocumentSignatures() {
		return Collections.unmodifiableList(signatures);
	}

	public boolean canVerifySignatures(Map<HexDigest, KeyCertificate> certificates) {
		for(DirectorySignature s : signatures) {
			KeyCertificate cert = certificates.get(s.getIdentityDigest());
			if(cert == null || !s.getSigningKeyDigest().equals(cert.getAuthoritySigningKey().getFingerprint()))
				return false;
		}
		return true;
	}

	public boolean verifySignatures(Map<HexDigest, KeyCertificate> certificates) {
		for(DirectorySignature s : signatures) {
			KeyCertificate cert = certificates.get(s.getIdentityDigest());
			if(cert == null) return false;
			TorPublicKey signingKey = cert.getAuthoritySigningKey();
			if(!signingKey.verifySignature(s.getSignature(), signingHash))
				return false;
		}
		return true;
	}

	public boolean equals(Object o) {
		if(!(o instanceof ConsensusDocumentImpl))
			return false;
		final ConsensusDocumentImpl other = (ConsensusDocumentImpl) o;
		return other.getSigningHash().equals(signingHash);
	}

	public int hashCode() {
		return signingHash.hashCode();
	}


}
