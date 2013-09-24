package com.subgraph.orchid.directory.parsing;

import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;

import com.subgraph.orchid.ConsensusDocument;
import com.subgraph.orchid.KeyCertificate;
import com.subgraph.orchid.RouterDescriptor;
import com.subgraph.orchid.RouterMicrodescriptor;

public interface DocumentParserFactory {
	DocumentParser<RouterDescriptor> createRouterDescriptorParser(InputStream input, boolean verifySignatures);
	DocumentParser<RouterDescriptor> createRouterDescriptorParser(Reader reader, boolean verifySignature);
	
	DocumentParser<RouterMicrodescriptor> createRouterMicrodescriptorParser(ByteBuffer buffer);
	DocumentParser<RouterMicrodescriptor> createRouterMicrodescriptorParser(InputStream input);
	DocumentParser<RouterMicrodescriptor> createRouterMicrodescriptorParser(Reader reader);

	DocumentParser<KeyCertificate> createKeyCertificateParser(InputStream input);
	DocumentParser<KeyCertificate> createKeyCertificateParser(Reader reader);

	DocumentParser<ConsensusDocument> createConsensusDocumentParser(InputStream input);
	DocumentParser<ConsensusDocument> createConsensusDocumentParser(Reader reader);

	DocumentFieldParser createDocumentFieldParser(InputStream input);
	DocumentFieldParser createDocumentFieldParser(Reader reader);
}
