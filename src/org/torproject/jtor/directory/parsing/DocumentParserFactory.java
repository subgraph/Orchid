package org.torproject.jtor.directory.parsing;

import java.io.InputStream;
import java.io.Reader;

import org.torproject.jtor.directory.KeyCertificate;
import org.torproject.jtor.directory.RouterDescriptor;
import org.torproject.jtor.directory.StatusDocument;

public interface DocumentParserFactory {
	DocumentParser<RouterDescriptor> createRouterDescriptorParser(InputStream input);
	DocumentParser<RouterDescriptor> createRouterDescriptorParser(Reader reader);

	DocumentParser<KeyCertificate> createKeyCertificateParser(InputStream input);
	DocumentParser<KeyCertificate> createKeyCertificateParser(Reader reader);


	DocumentParser<StatusDocument> createStatusDocumentParser(InputStream input);
	DocumentParser<StatusDocument> createStatusDocumentParser(Reader reader);
	
	DocumentFieldParser createDocumentFieldParser(InputStream input);
	DocumentFieldParser createDocumentFieldParser(Reader reader);


}
