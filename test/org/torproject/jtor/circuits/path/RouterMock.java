package org.torproject.jtor.circuits.path;

import java.util.Set;

import org.torproject.jtor.crypto.TorPublicKey;
import org.torproject.jtor.data.HexDigest;
import org.torproject.jtor.data.IPv4Address;
import org.torproject.jtor.directory.Router;
import org.torproject.jtor.directory.RouterDescriptor;

public class RouterMock implements Router {

	String name;
	IPv4Address address;
	HexDigest identity;
	String country;
	
	public String getNickname() {
		return name;
	}

	public String getCountryCode() {
		return country;
	}

	public IPv4Address getAddress() {
		return address;
	}

	public int getOnionPort() {
		return 0;
	}

	public int getDirectoryPort() {
		return 0;
	}

	public TorPublicKey getIdentityKey() {
		return null;
	}

	public HexDigest getIdentityHash() {
		return identity;
	}

	public boolean isDescriptorDownloadable() {
		return false;
	}

	public String getVersion() {
		return null;
	}

	public RouterDescriptor getCurrentDescriptor() {
		return null;
	}

	public HexDigest getDescriptorDigest() {
		return null;
	}

	public TorPublicKey getOnionKey() {
		return null;
	}

	public boolean hasBandwidth() {
		return false;
	}

	public int getEstimatedBandwidth() {
		return 0;
	}

	public int getMeasuredBandwidth() {
		return 0;
	}

	public Set<String> getFamilyMembers() {
		return null;
	}

	public int getAverageBandwidth() {
		return 0;
	}

	public int getBurstBandwidth() {
		return 0;
	}

	public int getObservedBandwidth() {
		return 0;
	}

	public boolean isHibernating() {
		return false;
	}

	public boolean isRunning() {
		return false;
	}

	public boolean isValid() {
		return false;
	}

	public boolean isBadExit() {
		return false;
	}

	public boolean isPossibleGuard() {
		return false;
	}

	public boolean isExit() {
		return false;
	}

	public boolean isFast() {
		return false;
	}

	public boolean isStable() {
		return false;
	}

	public boolean isHSDirectory() {
		return false;
	}

	public boolean exitPolicyAccepts(IPv4Address address, int port) {
		return false;
	}

	public boolean exitPolicyAccepts(int port) {
		return false;
	}
}
