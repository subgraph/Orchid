package org.torproject.jtor.directory;

import java.util.List;

public interface DirectoryStore {
	void loadCertificates(Directory directory);
	void saveCertificates(List<KeyCertificate> certificates);
	void loadConsensus(Directory directory);
	void saveConsensus(StatusDocument consensus);
	void saveRouterDescriptors(List<RouterDescriptor> descriptors);
	void loadRouterDescriptors(Directory directory);
}
