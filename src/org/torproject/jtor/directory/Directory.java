package org.torproject.jtor.directory;

import java.util.Collection;
import java.util.List;

import org.torproject.jtor.data.HexDigest;

/**
 * 
 * Main interface for accessing directory information and interacting
 * with directory authorities and caches.
 *
 */
public interface Directory {
	void loadFromStore();
	void storeCertificates();
	void storeConsensus();
	void storeDescriptors();
	Collection<DirectoryServer> getDirectoryAuthorities();
	DirectoryServer getRandomDirectoryAuthority();
	Router getRandomDirectoryServer();
	void addCertificate(KeyCertificate certificate);
	void addRouterDescriptor(RouterDescriptor router);
	void addConsensusDocument(StatusDocument consensus);
	StatusDocument getCurrentConsensusDocument();
	KeyCertificate findCertificate(HexDigest authorityFingerprint);
	Router getRouterByName(String name);
	List<Router> getRouterListByNames(List<String> names);
	List<Router> getRoutersWithDownloadableDescriptors();
	void markDescriptorInvalid(RouterDescriptor descriptor);

}
