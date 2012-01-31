package org.torproject.jtor.directory.impl.consensus;

import org.torproject.jtor.data.HexDigest;
import org.torproject.jtor.data.IPv4Address;
import org.torproject.jtor.directory.VoteAuthorityEntry;

public class VoteAuthorityEntryImpl implements VoteAuthorityEntry {
	private String nickname;
	private HexDigest identity;
	private String hostname;
	private IPv4Address address;
	private int dirport = -1;
	private int orport = -1;
	
	private String contact;
	private HexDigest voteDigest;
	
	void setNickname(String nickname) { this.nickname = nickname; }
	void setIdentity(HexDigest identity) { this.identity = identity; }
	void setHostname(String hostname) { this.hostname = hostname; }
	void setAddress(IPv4Address address) { this.address = address; }
	void setDirectoryPort(int port) { this.dirport = port; }
	void setRouterPort(int port) { this.orport = port; }
	void setContact(String contact) { this.contact = contact; }
	void setVoteDigest(HexDigest digest) { this.voteDigest = digest; }

	public String getNickname() {
		return nickname;
	}
	
	public HexDigest getIdentity() {
		return identity;
	}
	
	public String getHostname() {
		return hostname;
	}
	
	public IPv4Address getAddress() {
		return address;
	}
	
	public int getDirectoryPort() {
		return dirport;
	}
	
	public int getRouterPort() {
		return orport;
	}
	
	public String getContact() {
		return contact;
	}
	
	public HexDigest getVoteDigest() {
		return voteDigest;
	}
}
