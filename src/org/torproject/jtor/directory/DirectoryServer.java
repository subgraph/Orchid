package org.torproject.jtor.directory;

import org.torproject.jtor.data.HexDigest;
import org.torproject.jtor.data.IPv4Address;

/**
 * Represents a directory authority server or a directory cache.
 */
public interface DirectoryServer {
	IPv4Address getAddress();
	int getDirectoryPort();
	boolean isTrustedAuthority();
	HexDigest getFingerprint();
	String getNickname();
	boolean isV2Authority();
	boolean isV3Authority();
	boolean isHiddenServiceAuthority();
	boolean isBridgeAuthority();
	boolean isExtraInfoCache();

}
