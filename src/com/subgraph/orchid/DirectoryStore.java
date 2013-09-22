package com.subgraph.orchid;

import java.nio.ByteBuffer;
import java.util.List;

public interface DirectoryStore {
	void loadCertificates(Directory directory);
	void saveCertificates(List<KeyCertificate> certificates);
	void loadConsensus(Directory directory);
	void saveConsensus(ConsensusDocument consensus);
	void saveRouterDescriptors(List<RouterDescriptor> descriptors);
	void loadRouterDescriptors(Directory directory);

	void writeMicrodescriptorCache(List<RouterMicrodescriptor> descriptors, boolean removeJournal);
	void appendMicrodescriptorsToJournal(List<RouterMicrodescriptor> descriptors);
	ByteBuffer[] loadMicrodescriptorCache();
}
