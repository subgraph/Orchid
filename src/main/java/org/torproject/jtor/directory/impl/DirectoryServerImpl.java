package org.torproject.jtor.directory.impl;

import org.torproject.jtor.data.HexDigest;
import org.torproject.jtor.directory.DirectoryServer;
import org.torproject.jtor.directory.RouterStatus;

public class DirectoryServerImpl extends RouterImpl implements DirectoryServer {
	private boolean isHiddenServiceAuthority = false;
	private boolean isBridgeAuthority = false;
	private boolean isExtraInfoCache = false;
	private int port;
	private HexDigest v3Ident;
	
	DirectoryServerImpl(RouterStatus status) {
		super(status);
	}
	
	void setHiddenServiceAuthority() { isHiddenServiceAuthority = true; }
	void unsetHiddenServiceAuthority() { isHiddenServiceAuthority = false; }
	void setBridgeAuthority() { isBridgeAuthority = true; }
	void setExtraInfoCache() { isExtraInfoCache = true; }
	void setPort(int port) { this.port = port; }
	void setV3Ident(HexDigest fingerprint) { this.v3Ident = fingerprint; }
	
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
	
	public boolean isV2Authority() {
		return hasFlag("Authority") && hasFlag("V2Dir");
	}
	
	public boolean isV3Authority() {
		return hasFlag("Authority") && v3Ident != null;
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
	
	public HexDigest getV3Identity() {
		return v3Ident;
	}
	
	public String toString() {
		if(v3Ident != null) 
			return "(Directory: "+ getNickname() +" "+ getAddress() +":"+ port +" fingerprint="+ getIdentityHash() +" v3ident="+ 
				v3Ident +")";
		else
			return "(Directory: "+ getNickname() +" "+ getAddress() +":"+ port +" fingerprint="+ getIdentityHash() +")";

	}
}
