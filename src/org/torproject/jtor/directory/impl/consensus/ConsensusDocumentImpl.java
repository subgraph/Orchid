package org.torproject.jtor.directory.impl.consensus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.torproject.jtor.ConsensusDocument;
import org.torproject.jtor.KeyCertificate;
import org.torproject.jtor.RouterStatus;
import org.torproject.jtor.VoteAuthorityEntry;
import org.torproject.jtor.crypto.TorPublicKey;
import org.torproject.jtor.data.HexDigest;
import org.torproject.jtor.data.Timestamp;

public class ConsensusDocumentImpl implements ConsensusDocument {
	
	private final static String BW_WEIGHT_SCALE_PARAM = "bwweightscale";
	private final static int BW_WEIGHT_SCALE_DEFAULT = 10000;
	private final static int BW_WEIGHT_SCALE_MIN = 1;
	private final static int BW_WEIGHT_SCALE_MAX = Integer.MAX_VALUE;
	
	private final static String CIRCWINDOW_PARAM = "circwindow";
	private final static int CIRCWINDOW_DEFAULT = 1000;
	private final static int CIRCWINDOW_MIN = 100;
	private final static int CIRCWINDOW_MAX = 1000;
	
	
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
	private Map<String, Integer> bandwidthWeights;
	private Map<String, Integer> parameters;
	
	private String rawDocumentData;
	
	void setConsensusMethod(int method) { consensusMethod = method; }
	void setValidAfter(Timestamp ts) { validAfter = ts; }
	void setFreshUntil(Timestamp ts) { freshUntil = ts; }
	void setValidUntil(Timestamp ts) { validUntil = ts; }
	void setDistDelaySeconds(int seconds) { distDelaySeconds = seconds; }
	void setVoteDelaySeconds(int seconds) { voteDelaySeconds = seconds; }
	void addClientVersion(String version) { clientVersions.add(version); }
	void addServerVersion(String version) { serverVersions.add(version); }
	void addParameter(String name, int value) { parameters.put(name, value); }
	void addBandwidthWeight(String name, int value) { bandwidthWeights.put(name, value); }
		
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
		bandwidthWeights = new HashMap<String, Integer>();
		parameters = new HashMap<String, Integer>();
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
		if(validUntil == null) {
			return false;
		} else {
			return !validUntil.hasPassed(); 
		}
	}
	
	public List<RouterStatus> getRouterStatusEntries() {
		return Collections.unmodifiableList(routerStatusEntries);
	}
	
	public String getRawDocumentData() {
		return rawDocumentData;
	}
	
	public boolean isValidDocument() {
		return (validAfter != null) && (freshUntil != null) && (validUntil != null) &&
		(voteDelaySeconds > 0) && (distDelaySeconds > 0) && (signingHash != null) &&
		(signatures.size() != 0);
	}
	
	public HexDigest getSigningHash() {
		return signingHash;
	}
	
	public List<DirectorySignature> getDocumentSignatures() {
		return Collections.unmodifiableList(signatures);
	}
	
	public boolean canVerifySignatures(Map<HexDigest, KeyCertificate> certificates) {
		for(DirectorySignature s: signatures) {
			KeyCertificate cert = certificates.get(s.getIdentityDigest());
			if(cert == null || !s.getSigningKeyDigest().equals(cert.getAuthoritySigningKey().getFingerprint()))
				return false;
		}
		return true;
	}
	
	public boolean verifySignatures(Map<HexDigest, KeyCertificate> certificates) {
		for(DirectorySignature s: signatures) {
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
		return (signingHash == null) ? 0 : signingHash.hashCode();
	}
	
	private int getParameterValue(String name, int defaultValue, int minValue, int maxValue) {
		if(!parameters.containsKey(name)) {
			return defaultValue;
		}
		final int value = parameters.get(name);
		if(value < minValue) {
			return minValue;
		} else if(value > maxValue) {
			return maxValue;
		} else {
			return value;
		}
	}
	
	public int getCircWindowParameter() {
		return getParameterValue(CIRCWINDOW_PARAM, CIRCWINDOW_DEFAULT, CIRCWINDOW_MIN, CIRCWINDOW_MAX);
	}
	
	public int getWeightScaleParameter() {
		return getParameterValue(BW_WEIGHT_SCALE_PARAM, BW_WEIGHT_SCALE_DEFAULT, BW_WEIGHT_SCALE_MIN, BW_WEIGHT_SCALE_MAX);
	}
	
	public int getBandwidthWeight(String tag) {
		if(bandwidthWeights.containsKey(tag)) {
			return bandwidthWeights.get(tag);
		} else {
			return -1;
		}
	}
}
