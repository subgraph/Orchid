package org.torproject.jtor.directory;

import org.torproject.jtor.crypto.TorPublicKey;
import org.torproject.jtor.data.HexDigest;
import org.torproject.jtor.data.IPv4Address;

public interface Router {

	String getNickname();
	IPv4Address getAddress();
	int getOnionPort();
	int getDirectoryPort();
	TorPublicKey getIdentityKey();
	HexDigest getIdentityHash();
	boolean isDescriptorDownloadable();

	RouterDescriptor getCurrentDescriptor();
	HexDigest getDescriptorDigest();
	TorPublicKey getOnionKey();
	int getEstimatedBandwidth();
	int getMeasuredBandwidth();

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
	boolean exitPolicyAccepts(IPv4Address address, int port);
	boolean exitPolicyAccepts(int port);
}
