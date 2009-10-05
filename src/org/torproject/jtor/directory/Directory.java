package org.torproject.jtor.directory;

import java.util.Collection;

import org.torproject.jtor.data.HexDigest;

/**
 * 
 * Main interface for accessing directory information and interacting
 * with directory authorities and caches.
 *
 */
public interface Directory {
	Collection<DirectoryServer> getDirectoryAuthorities();
	DirectoryServer getRandomDirectoryAuthority();
	void addCertificate(KeyCertificate certificate);
	void addRouterDescriptor(RouterDescriptor router);
	void addConsensusDocument(StatusDocument consensus);
	StatusDocument getCurrentConsensusDocument();
	KeyCertificate findCertificate(HexDigest authorityFingerprint);
}
