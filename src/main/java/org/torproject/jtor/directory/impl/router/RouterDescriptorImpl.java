package org.torproject.jtor.directory.impl.router;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.torproject.jtor.crypto.TorPublicKey;
import org.torproject.jtor.data.BandwidthHistory;
import org.torproject.jtor.data.HexDigest;
import org.torproject.jtor.data.IPv4Address;
import org.torproject.jtor.data.Timestamp;
import org.torproject.jtor.data.exitpolicy.ExitPolicy;
import org.torproject.jtor.directory.RouterDescriptor;

public class RouterDescriptorImpl implements RouterDescriptor {
	private String nickname;
	private IPv4Address address;
	private int routerPort;
	private int directoryPort;

	private int averageBandwidth = -1;
	private int burstBandwidth = -1;
	private int observedBandwidth = -1;

	private String platform;

	private Timestamp published;

	private HexDigest fingerprint;

	private boolean hibernating;

	private int uptime;

	private TorPublicKey onionKey;
	private TorPublicKey identityKey;
	private ExitPolicy exitPolicy = new ExitPolicy();

	private String contact;
	private Set<String> families = Collections.emptySet();
	private Set<Integer> linkProtocols = Collections.emptySet();
	private Set<Integer> circuitProtocols = Collections.emptySet();

	private BandwidthHistory readHistory;
	private BandwidthHistory writeHistory;

	private boolean eventDNS = false;
	private boolean cachesExtraInfo = false;
	private boolean hiddenServiceDir = false;
	private HexDigest extraInfoDigest = null;
	private boolean allowSingleHopExits = false;
	private boolean hasValidSignature = false;

	private HexDigest descriptorDigest;
	private String rawDocumentData;
	
	public void setNickname(String nickname) { this.nickname = nickname; }
	public void setAddress(IPv4Address address) { this.address = address; }
	public void setRouterPort(int port) { this.routerPort = port; }
	void setDirectoryPort(int port) { this.directoryPort = port; }
	void setPlatform(String platform) { this.platform = platform; }
	void setPublished(Timestamp published) { this.published = published; }
	void setFingerprint(HexDigest fingerprint) { this.fingerprint = fingerprint; }
	void setHibernating(boolean flag) { this.hibernating = flag; }
	void setUptime(int uptime) { this.uptime = uptime; }
	public void setOnionKey(TorPublicKey key) { this.onionKey = key; }
	void setIdentityKey(TorPublicKey key) { this.identityKey = key; }
	void setContact(String contact) { this.contact = contact; }
	void setEventDNS() { eventDNS = true; }
	void setHiddenServiceDir() { hiddenServiceDir = true; }
	void setExtraInfoDigest(HexDigest digest) { this.extraInfoDigest = digest; }
	void setCachesExtraInfo() { cachesExtraInfo = true; }
	void setAllowSingleHopExits() { allowSingleHopExits = true; }
	void setReadHistory(BandwidthHistory history) { this.readHistory= history; }
	void setWriteHistory(BandwidthHistory history) { this.writeHistory = history; }
	void setValidSignature() { hasValidSignature = true; }
	void setDescriptorHash(HexDigest digest) { descriptorDigest = digest; }
	void setRawDocumentData(String rawData) { rawDocumentData = rawData; }

	void addAcceptRule(String rule) {
		exitPolicy.addAcceptRule(rule);
	}

	void addRejectRule(String rule) {
		exitPolicy.addRejectRule(rule);
	}

	void setBandwidthValues(int average, int burst, int observed) {
		this.averageBandwidth = average;
		this.burstBandwidth = burst;
		this.observedBandwidth = observed;
	}

	void addFamily(String family) {
		if(families.isEmpty())
			families = new HashSet<String>();
		families.add(family);
	}

	void addCircuitProtocolVersion(int version) {
		if(circuitProtocols.isEmpty())
			circuitProtocols = new HashSet<Integer>();
		circuitProtocols.add(version);
	}

	void addLinkProtocolVersion(int version) {
		if(linkProtocols.isEmpty())
			linkProtocols = new HashSet<Integer>();
		linkProtocols.add(version);	
	}

	public boolean isValidDocument() {
		// verify required fields exist, see dirspec.txt section 2.1
		return hasValidSignature && (nickname != null) && (address != null) &&
			(averageBandwidth != -1) && (routerPort != 0 || directoryPort != 0) &&
			(published != null) && (onionKey != null) && (identityKey != null) &&
			(descriptorDigest != null);
	}

	public String getNickname() {
		return nickname;
	}

	public IPv4Address getAddress() {
		return address;
	}

	public int getRouterPort() {
		return routerPort;
	}

	public int getDirectoryPort() {
		return directoryPort;
	}

	public int getAverageBandwidth() {
		return averageBandwidth;
	}

	public int getBurstBandwidth() {
		return burstBandwidth;
	}

	public int getObservedBandwidth() {
		return observedBandwidth;
	}

	public String getPlatform() {
		return platform;
	}

	public HexDigest getFingerprint() {
		return fingerprint;
	}

	public int getUptime() {
		return uptime;
	}

	public TorPublicKey getOnionKey() {
		return onionKey;
	}

	public TorPublicKey getIdentityKey() {
		return identityKey;
	}

	public String getContact() {
		return contact;
	}

	public boolean isHibernating() {
		return hibernating;
	}

	public boolean cachesExtraInfo() {
		return cachesExtraInfo;
	}

	public boolean allowsSingleHopExits() {
		return allowSingleHopExits;
	}

	public Timestamp getPublishedTime() {
		return published;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Router Descriptor: (name: ");
		builder.append(nickname);
		builder.append(" orport=");
		builder.append(routerPort);
		builder.append(" dirport=");
		builder.append(directoryPort);
		builder.append(" address=");
		builder.append(address);
		builder.append(" platform=");
		builder.append(platform);
		builder.append(" published=");
		builder.append(published.getDate());
		builder.append(")");
		return builder.toString();
	}

	public void print() {
		System.out.println("nickname: "+ nickname +" IP: "+ address +" port: "+ routerPort);
		System.out.println("directory port: "+ directoryPort +" platform: "+ platform);
		System.out.println("Bandwidth(avg/burst/observed): "+ averageBandwidth +"/"+ burstBandwidth +"/"+ observedBandwidth);
		System.out.println("Publication time: "+ published +" Uptime: "+ uptime);
		if(fingerprint != null)
			System.out.println("Fingerprint: "+ fingerprint);
		if(contact != null)
			System.out.println("Contact: "+ contact);
	}
	public boolean exitPolicyAccepts(IPv4Address address, int port) {
		return exitPolicy.acceptsDestination(address, port);
	}

	public boolean exitPolicyAccepts(int port) {
		return exitPolicy.acceptsPort(port);
	}

	public HexDigest getExtraInfoDigest() {
		return extraInfoDigest;
	}

	public boolean isHiddenServiceDirectory() {
		return hiddenServiceDir;
	}

	public boolean isInFamily(String family) {
		return families.contains(family);
	}

	public boolean supportsEventDNS() {
		return eventDNS;
	}

	public BandwidthHistory getReadHistory() {
		return readHistory;
	}

	public BandwidthHistory getWriteHistory() {
		return writeHistory;
	}

	public boolean isNewerThan(RouterDescriptor other) {
		return other.getPublishedTime().isBefore(published);
	}
	
	public HexDigest getDescriptorDigest() {
		return descriptorDigest;
	}
	
	public String getRawDocumentData() {
		return rawDocumentData;
	}
	
	public boolean equals(Object o) {
		if(!(o instanceof RouterDescriptorImpl)) 
			return false;
		final RouterDescriptorImpl other = (RouterDescriptorImpl) o;
		if(other.getDescriptorDigest() == null || descriptorDigest == null)
			return false;

		return other.getDescriptorDigest().equals(descriptorDigest);
	}

	public int hashCode() {
		if(descriptorDigest == null)
			return 0;
		return descriptorDigest.hashCode();
	}
}
