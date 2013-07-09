package com.subgraph.orchid.circuits.hs;

import java.io.StringReader;

import com.subgraph.orchid.TorParsingException;
import com.subgraph.orchid.crypto.TorSignature;
import com.subgraph.orchid.directory.DocumentFieldParserImpl;
import com.subgraph.orchid.directory.parsing.DocumentFieldParser;
import com.subgraph.orchid.directory.parsing.DocumentObject;
import com.subgraph.orchid.directory.parsing.DocumentParser;
import com.subgraph.orchid.directory.parsing.DocumentParsingHandler;
import com.subgraph.orchid.directory.parsing.DocumentParsingResultHandler;
import com.subgraph.orchid.encoders.Base64;

public class HSDescriptorParser implements DocumentParser<HSDescriptor>{

	private final DocumentFieldParser fieldParser;
	private final HSDescriptor descriptor;
	
	private DocumentParsingResultHandler<HSDescriptor> resultHandler;
	
	public HSDescriptorParser(DocumentFieldParser fieldParser) {
		this.fieldParser = fieldParser;
		this.fieldParser.setHandler(createParsingHandler());
		this.descriptor = new HSDescriptor();
	}
	
	private DocumentParsingHandler createParsingHandler() {
		return new DocumentParsingHandler() {
			
			public void parseKeywordLine() {
				processKeywordLine();
			}
			
			public void endOfDocument() {
			}
		};
	}

	public boolean parse(DocumentParsingResultHandler<HSDescriptor> resultHandler) {
		this.resultHandler = resultHandler;
		fieldParser.startSignedEntity();
		try {
			fieldParser.processDocument();
			return true;
		} catch(TorParsingException e) {
			resultHandler.parsingError(e.getMessage());
			return false;
		}
	}
	
	
	private void processKeywordLine() {
		final HSDescriptorKeyword keyword = HSDescriptorKeyword.findKeyword(fieldParser.getCurrentKeyword());
		if(!keyword.equals(HSDescriptorKeyword.UNKNOWN_KEYWORD)) {
			processKeyword(keyword);
		}
	}
	
	private void processKeyword(HSDescriptorKeyword keyword) {
		switch(keyword) {
		case RENDEZVOUS_SERVICE_DESCRIPTOR:
			descriptor.setDescriptorId(fieldParser.parseBase32Digest());
			break;
		case VERSION:
			if(fieldParser.parseInteger() != 2) {
				throw new TorParsingException("Unexpected Descriptor version");
			}
			break;
			
		case PERMANENT_KEY:
			descriptor.setPermanentKey(fieldParser.parsePublicKey());
			break;
			
		case SECRET_ID_PART:
			descriptor.setSecretIdPart(fieldParser.parseBase32Digest());
			break;
			
		case PUBLICATION_TIME:
			descriptor.setPublicationTime(fieldParser.parseTimestamp());
			break;
			
		case PROTOCOL_VERSIONS:
			descriptor.setProtocolVersions(fieldParser.parseIntegerList());
			break;
			
		case INTRODUCTION_POINTS:
			processIntroductionPoints();
			break;
			
		case SIGNATURE:
			processSignature();
			break;
		case UNKNOWN_KEYWORD:
			break;
		}
	}
	
	private void processIntroductionPoints() {
		final DocumentObject ob = fieldParser.parseObject();
		final String decoded = new String(Base64.decode(ob.getContent(false)));
		System.out.println(decoded);
		final StringReader reader = new StringReader(decoded);
		final IntroductionPointParser parser = new IntroductionPointParser(new DocumentFieldParserImpl(reader));
		parser.parse(new DocumentParsingResultHandler<IntroductionPoint>() {

			public void documentParsed(IntroductionPoint document) {
				descriptor.addIntroductionPoint(document);
			}

			public void documentInvalid(IntroductionPoint document, String message) {
				// TODO Auto-generated method stub
				
			}

			public void parsingError(String message) {
				// TODO Auto-generated method stub
				
			} 
			
		});
	}

	private void processSignature() {
		fieldParser.endSignedEntity();
		final TorSignature signature = fieldParser.parseSignature();
		if(!fieldParser.verifySignedEntity(descriptor.getPermanentKey(), signature)) {
			resultHandler.documentInvalid(descriptor, "Signature verification failed");
			fieldParser.logWarn("Signature failed for descriptor: "+ descriptor.getDescriptorId().toBase32());
			return;
		}
		System.out.println("SIGNATURE OK");
		resultHandler.documentParsed(descriptor);
	}
}
