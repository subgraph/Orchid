package com.subgraph.orchid.directory;

import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;

import com.subgraph.orchid.ConsensusDocument;
import com.subgraph.orchid.KeyCertificate;
import com.subgraph.orchid.RouterDescriptor;
import com.subgraph.orchid.RouterMicrodescriptor;
import com.subgraph.orchid.directory.certificate.KeyCertificateParser;
import com.subgraph.orchid.directory.consensus.ConsensusDocumentParser;
import com.subgraph.orchid.directory.parsing.DocumentFieldParser;
import com.subgraph.orchid.directory.parsing.DocumentParser;
import com.subgraph.orchid.directory.parsing.DocumentParserFactory;
import com.subgraph.orchid.directory.router.RouterDescriptorParser;
import com.subgraph.orchid.directory.router.RouterMicrodescriptorParser;

public class DocumentParserFactoryImpl implements DocumentParserFactory {
	
	public DocumentParser<KeyCertificate> createKeyCertificateParser(InputStream input) {
		return new KeyCertificateParser(createDocumentFieldParser(input));
	}

	public DocumentParser<KeyCertificate> createKeyCertificateParser(Reader reader) {
		return new KeyCertificateParser(createDocumentFieldParser(reader));
	}

	public DocumentParser<RouterDescriptor> createRouterDescriptorParser(InputStream input, boolean verifySignatures) {
		return new RouterDescriptorParser(createDocumentFieldParser(input), verifySignatures);
	}

	public DocumentParser<RouterDescriptor> createRouterDescriptorParser(Reader reader, boolean verifySignatures) {
		return new RouterDescriptorParser(createDocumentFieldParser(reader), verifySignatures);
	}

	public DocumentParser<RouterMicrodescriptor> createRouterMicrodescriptorParser(ByteBuffer buffer) {
		DocumentFieldParser dfp = new DocumentFieldParserImpl(buffer);
		return new RouterMicrodescriptorParser(dfp);
	}

	public DocumentParser<RouterMicrodescriptor> createRouterMicrodescriptorParser(InputStream input) {
		return new RouterMicrodescriptorParser(createDocumentFieldParser(input));
	}

	public DocumentParser<RouterMicrodescriptor> createRouterMicrodescriptorParser(Reader reader) {
		return new RouterMicrodescriptorParser(createDocumentFieldParser(reader));
	}

	public DocumentParser<ConsensusDocument> createConsensusDocumentParser(InputStream input) {
		return new ConsensusDocumentParser(createDocumentFieldParser(input));
	}

	public DocumentParser<ConsensusDocument> createConsensusDocumentParser(Reader reader) {
		return new ConsensusDocumentParser(createDocumentFieldParser(reader));
	}
	
	public DocumentFieldParser createDocumentFieldParser(InputStream input) {
		return new DocumentFieldParserImpl(input);
	}
	
	public DocumentFieldParser createDocumentFieldParser(Reader reader) {
		return new DocumentFieldParserImpl(reader);
	}
	
}
