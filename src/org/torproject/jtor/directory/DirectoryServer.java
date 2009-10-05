package org.torproject.jtor.directory;

import org.torproject.jtor.data.HexDigest;

/**
 * Represents a directory authority server or a directory cache.
 */
public interface DirectoryServer {
	boolean isTrustedAuthority();
	HexDigest getFingerprint();
	String getNickname();
	boolean isV2Authority();
	boolean isV3Authority();
	boolean isHiddenServiceAuthority();
	boolean isBridgeAuthority();
	boolean isExtraInfoCache();

}
