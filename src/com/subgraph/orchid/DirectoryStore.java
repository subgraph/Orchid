package com.subgraph.orchid;

import java.nio.ByteBuffer;
import java.util.List;

public interface DirectoryStore {
	ByteBuffer loadCertificates();
	void saveCertificates(List<KeyCertificate> certificates);
	ByteBuffer loadConsensus();
	void saveConsensus(ConsensusDocument consensus);
	void saveRouterDescriptors(List<RouterDescriptor> descriptors);
	ByteBuffer loadRouterDescriptors();

	void writeMicrodescriptorCache(List<RouterMicrodescriptor> descriptors, boolean removeJournal);
	void appendMicrodescriptorsToJournal(List<RouterMicrodescriptor> descriptors);
	ByteBuffer[] loadMicrodescriptorCache();
}
