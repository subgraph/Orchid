package com.subgraph.orchid.directory.parsing;

import java.io.InputStream;
import java.io.Reader;

import com.subgraph.orchid.ConsensusDocument;
import com.subgraph.orchid.KeyCertificate;
import com.subgraph.orchid.RouterDescriptor;

public interface DocumentParserFactory {
	DocumentParser<RouterDescriptor> createRouterDescriptorParser(InputStream input);
	DocumentParser<RouterDescriptor> createRouterDescriptorParser(Reader reader);

	DocumentParser<KeyCertificate> createKeyCertificateParser(InputStream input);
	DocumentParser<KeyCertificate> createKeyCertificateParser(Reader reader);

	DocumentParser<ConsensusDocument> createConsensusDocumentParser(InputStream input);
	DocumentParser<ConsensusDocument> createConsensusDocumentParser(Reader reader);

	DocumentFieldParser createDocumentFieldParser(InputStream input);
	DocumentFieldParser createDocumentFieldParser(Reader reader);
}
