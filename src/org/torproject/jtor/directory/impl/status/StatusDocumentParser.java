package org.torproject.jtor.directory.impl.status;

import java.util.Arrays;
import java.util.List;

import org.torproject.jtor.directory.Directory;
import org.torproject.jtor.directory.StatusDocument;
import org.torproject.jtor.directory.parsing.DocumentFieldParser;
import org.torproject.jtor.directory.parsing.DocumentParser;
import org.torproject.jtor.directory.parsing.DocumentParsingHandler;

public class StatusDocumentParser implements DocumentParser<StatusDocument> {
	public enum DocumentSection { NO_SECTION, PREAMBLE, AUTHORITY, ROUTER_STATUS, SIGNATURE };

	// dir-spec.txt 3.2 
	// Unlike other formats described above, a SP in these documents must be a
	// single space character (hex 20).
	private final static String ITEM_DELIMITER = " ";
	
	private final PreambleSectionParser preambleParser;
	private final AuthoritySectionParser authorityParser;
	private final RouterStatusSectionParser routerStatusParser;
	private final SignatureSectionParser signatureParser;
	private final DocumentFieldParser fieldParser;
	private DocumentSection currentSection = DocumentSection.PREAMBLE;
	private final StatusDocumentImpl document;
	
	public StatusDocumentParser(DocumentFieldParser fieldParser) {
		this.fieldParser = fieldParser;
		initializeParser();
		
		document = new StatusDocumentImpl();
		preambleParser = new PreambleSectionParser(fieldParser, document);
		authorityParser = new AuthoritySectionParser(fieldParser, document);
		routerStatusParser = new RouterStatusSectionParser(fieldParser, document);
		signatureParser = new SignatureSectionParser(fieldParser, document);
	}
	
	private void initializeParser() {
		fieldParser.setHandler(createParsingHandler());
		fieldParser.setDelimiter(ITEM_DELIMITER);
		fieldParser.setSignatureIgnoreToken("directory-signature");
		fieldParser.startSignedEntity();
	}
	
	public void parse() {
		fieldParser.processDocument();
	}
	
	public void parseAndAddToDirectory(Directory directory) {
		parse();
		if(document.isConsensusDocument())
			directory.addConsensusDocument(document);
	}
	private DocumentParsingHandler createParsingHandler() {
		return new DocumentParsingHandler() {

			public void endOfDocument() {
				fieldParser.logDebug("Finished parsing status document.");				
			}
			public void parseKeywordLine() {
				processKeywordLine();	
			}
			
		};
	}
	private void processKeywordLine() {
		DocumentSection newSection = null;
		while(currentSection != DocumentSection.NO_SECTION) {
			switch(currentSection) {
			case PREAMBLE:
				newSection = preambleParser.parseKeywordLine();
				break;
			case AUTHORITY:
				newSection = authorityParser.parseKeywordLine();
				break;
			case ROUTER_STATUS:
				newSection = routerStatusParser.parseKeywordLine();
				break;
			case SIGNATURE:
				newSection = signatureParser.parseKeywordLine();
				break;
			}
			if(newSection == currentSection)
				return;
			
			currentSection = newSection;
		}	
	}
	
	public List<StatusDocument> getDocuments() {
		return Arrays.asList((StatusDocument)document);
	}
}
