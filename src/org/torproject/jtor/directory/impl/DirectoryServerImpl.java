package org.torproject.jtor.directory.impl;

import org.torproject.jtor.data.HexDigest;
import org.torproject.jtor.data.IPv4Address;
import org.torproject.jtor.directory.DirectoryServer;

public class DirectoryServerImpl implements DirectoryServer {
	private final String nickname;
	private boolean isV1Authority = false;
	private boolean isV2Authority = true;
	private boolean isV3Authority = false;
	private boolean isHiddenServiceAuthority = false;
	private boolean isBridgeAuthority = false;
	private boolean isExtraInfoCache = false;
	private IPv4Address address;
	private int port;
	private int orport = 0;
	private HexDigest v3Ident;
	private HexDigest fingerprint;
	
	DirectoryServerImpl(String nickname) {
		this.nickname = nickname;	
	}
	
	void setV1Authority() { isV1Authority = true; }
	void setV2Authority() {	isV2Authority = true; }
	void unsetV2Authority() { isV2Authority = false; }
	void setV3Authority() { isV3Authority = true; }
	void setHiddenServiceAuthority() { isHiddenServiceAuthority = true; }
	void unsetHiddenServiceAuthority() { isHiddenServiceAuthority = false; }
	void setBridgeAuthority() { isBridgeAuthority = true; }
	void setExtraInfoCache() { isExtraInfoCache = true; }
	void setFingerprint(HexDigest fingerprint) { this.fingerprint = fingerprint;}
	void setAddress(IPv4Address address) { this.address = address;}
	void setPort(int port) { this.port = port; }
	void setORPort(int port) { this.orport = port;}
	void setV3Ident(HexDigest fingerprint) { this.v3Ident = fingerprint;}
	
	public boolean isTrustedAuthority() {
		return true;
	}
	
	/**
	 * Return true if this DirectoryServer entry has
	 * complete and valid information.
	 * @return
	 */
	public boolean isValid() {
		return true;
	}
	public String getNickname() {
		return nickname;
	}
	
	public HexDigest getFingerprint() {
		return fingerprint;
	}
	
	public boolean isV1Authority() {
		return isV1Authority;
	}
	
	public boolean isV2Authority() {
		return isV2Authority;
	}
	
	public boolean isV3Authority() {
		return isV3Authority;
	}
	
	public boolean isHiddenServiceAuthority() {
		return isHiddenServiceAuthority;
	}
	
	public boolean isBridgeAuthority() {
		return isBridgeAuthority;
	}
	
	public boolean isExtraInfoCache() {
		return isExtraInfoCache;
	}
	
	public int getOnionRouterPort() {
		return orport;
	}
	
	public HexDigest getV3Identity() {
		return v3Ident;
	}
	
	public IPv4Address getAddress() {
		return address;
	}
	
	public int getDirectoryPort() {
		return port;
	}
	
	public String toString() {
		if(v3Ident != null) 
			return "(Directory: "+ nickname +" "+ address +":"+ port +" fingerprint="+ fingerprint +" v3ident="+ 
				v3Ident +")";
		else
			return "(Directory: "+ nickname +" "+ address +":"+ port +" fingerprint="+ fingerprint +")";

	}
}
