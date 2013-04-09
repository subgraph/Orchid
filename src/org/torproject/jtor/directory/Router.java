package org.torproject.jtor.directory;

import java.util.Set;

import org.torproject.jtor.crypto.TorPublicKey;
import org.torproject.jtor.data.HexDigest;
import org.torproject.jtor.data.IPv4Address;

public interface Router {

	String getNickname();
	String getCountryCode();
	IPv4Address getAddress();
	int getOnionPort();
	int getDirectoryPort();
	TorPublicKey getIdentityKey();
	HexDigest getIdentityHash();
	boolean isDescriptorDownloadable();

	String getVersion();
	RouterDescriptor getCurrentDescriptor();
	HexDigest getDescriptorDigest();
	TorPublicKey getOnionKey();
	
	boolean hasBandwidth();
	int getEstimatedBandwidth();
	int getMeasuredBandwidth();

	Set<String> getFamilyMembers();
	int getAverageBandwidth();
	int getBurstBandwidth();
	int getObservedBandwidth();
	boolean isHibernating();
	boolean isRunning();
	boolean isValid();
	boolean isBadExit();
	boolean isPossibleGuard();
	boolean isExit();
	boolean isFast();
	boolean isStable();
	boolean isHSDirectory();
	boolean exitPolicyAccepts(IPv4Address address, int port);
	boolean exitPolicyAccepts(int port);
}
