package org.torproject.jtor.directory.impl;

import java.io.InputStream;
import java.io.Reader;

import org.torproject.jtor.directory.KeyCertificate;
import org.torproject.jtor.directory.RouterDescriptor;
import org.torproject.jtor.directory.ConsensusDocument;
import org.torproject.jtor.directory.impl.certificate.KeyCertificateParser;
import org.torproject.jtor.directory.impl.consensus.ConsensusDocumentParser;
import org.torproject.jtor.directory.impl.router.RouterDescriptorParser;
import org.torproject.jtor.directory.parsing.DocumentFieldParser;
import org.torproject.jtor.directory.parsing.DocumentParser;
import org.torproject.jtor.directory.parsing.DocumentParserFactory;
import org.torproject.jtor.logging.LogManager;
import org.torproject.jtor.logging.Logger;

public class DocumentParserFactoryImpl implements DocumentParserFactory {
	private final Logger logger;
	
	public DocumentParserFactoryImpl(LogManager logManager) {
		this.logger = logManager.getLogger("document-parsing");
	}

	public DocumentParser<KeyCertificate> createKeyCertificateParser(InputStream input) {
		return new KeyCertificateParser(createDocumentFieldParser(input));
	}

	public DocumentParser<KeyCertificate> createKeyCertificateParser(Reader reader) {
		return new KeyCertificateParser(createDocumentFieldParser(reader));
	}

	public DocumentParser<RouterDescriptor> createRouterDescriptorParser(InputStream input) {
		return new RouterDescriptorParser(createDocumentFieldParser(input));
	}

	public DocumentParser<RouterDescriptor> createRouterDescriptorParser(Reader reader) {
		return new RouterDescriptorParser(createDocumentFieldParser(reader));
	}

	public DocumentParser<ConsensusDocument> createConsensusDocumentParser(InputStream input) {
		return new ConsensusDocumentParser(createDocumentFieldParser(input));
	}

	public DocumentParser<ConsensusDocument> createConsensusDocumentParser(Reader reader) {
		return new ConsensusDocumentParser(createDocumentFieldParser(reader));
	}
	
	public DocumentFieldParser createDocumentFieldParser(InputStream input) {
		return new DocumentFieldParserImpl(input, logger);
	}
	
	public DocumentFieldParser createDocumentFieldParser(Reader reader) {
		return new DocumentFieldParserImpl(reader, logger);
	}
	
}
