package org.torproject.jtor.directory.impl;

import java.io.InputStream;
import java.io.Reader;

import org.torproject.jtor.Logger;
import org.torproject.jtor.directory.KeyCertificate;
import org.torproject.jtor.directory.RouterDescriptor;
import org.torproject.jtor.directory.StatusDocument;
import org.torproject.jtor.directory.impl.certificate.KeyCertificateParser;
import org.torproject.jtor.directory.impl.router.RouterDescriptorParser;
import org.torproject.jtor.directory.impl.status.StatusDocumentParser;
import org.torproject.jtor.directory.parsing.DocumentFieldParser;
import org.torproject.jtor.directory.parsing.DocumentParser;
import org.torproject.jtor.directory.parsing.DocumentParserFactory;

public class DocumentParserFactoryImpl implements DocumentParserFactory {
	private final Logger logger;
	
	public DocumentParserFactoryImpl(Logger logger) {
		this.logger = logger;
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

	public DocumentParser<StatusDocument> createStatusDocumentParser(InputStream input) {
		return new StatusDocumentParser(createDocumentFieldParser(input));
	}

	public DocumentParser<StatusDocument> createStatusDocumentParser(Reader reader) {
		return new StatusDocumentParser(createDocumentFieldParser(reader));
	}
	
	public DocumentFieldParser createDocumentFieldParser(InputStream input) {
		return new DocumentFieldParserImpl(input, logger);
	}
	
	public DocumentFieldParser createDocumentFieldParser(Reader reader) {
		return new DocumentFieldParserImpl(reader, logger);
	}
	
}
