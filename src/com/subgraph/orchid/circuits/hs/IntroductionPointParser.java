package com.subgraph.orchid.circuits.hs;

import com.subgraph.orchid.TorParsingException;
import com.subgraph.orchid.data.HexDigest;
import com.subgraph.orchid.directory.parsing.DocumentFieldParser;
import com.subgraph.orchid.directory.parsing.DocumentParser;
import com.subgraph.orchid.directory.parsing.DocumentParsingHandler;
import com.subgraph.orchid.directory.parsing.DocumentParsingResultHandler;

public class IntroductionPointParser implements DocumentParser<IntroductionPoint>{

	private final DocumentFieldParser fieldParser;
	
	private DocumentParsingResultHandler<IntroductionPoint> resultHandler;
	private IntroductionPoint currentIntroductionPoint;
	
	public IntroductionPointParser(DocumentFieldParser fieldParser) {
		this.fieldParser = fieldParser;
		this.fieldParser.setHandler(createParsingHandler());
	}
	
	public boolean parse(DocumentParsingResultHandler<IntroductionPoint> resultHandler) {
		this.resultHandler = resultHandler;
		resetIntroductionPoint(null);
		try {
			fieldParser.processDocument();
			return true;
		} catch(TorParsingException e) {
			resultHandler.parsingError(e.getMessage());
			return false;
		}
	}

	private DocumentParsingHandler createParsingHandler() {
		return new DocumentParsingHandler() {
			public void parseKeywordLine() {
				processKeywordLine();
			}
			
			public void endOfDocument() {}
		};
	}

	private void resetIntroductionPoint(HexDigest identity) {
		currentIntroductionPoint = new IntroductionPoint(identity);
	}
	
	
	private void processKeywordLine() {
		final IntroductionPointKeyword keyword = IntroductionPointKeyword.findKeyword(fieldParser.getCurrentKeyword());
		if(!keyword.equals(IntroductionPointKeyword.UNKNOWN_KEYWORD)) {
			processKeyword(keyword);
		}
	}
	
	private void processKeyword(IntroductionPointKeyword keyword) {
		switch(keyword) {
		case INTRO_AUTHENTICATION:
			break;
			
		case INTRODUCTION_POINT:
			resetIntroductionPoint(fieldParser.parseBase32Digest());
			break;
			
		case IP_ADDRESS:
			currentIntroductionPoint.setAddress(fieldParser.parseAddress());
			break;
			
		case ONION_KEY:
			currentIntroductionPoint.setOnionKey(fieldParser.parsePublicKey());
			break;
			
		case ONION_PORT:
			currentIntroductionPoint.setOnionPort(fieldParser.parsePort());
			break;
			
		case SERVICE_KEY:
			currentIntroductionPoint.setServiceKey(fieldParser.parsePublicKey());
			break;
			
		case SERVICE_AUTHENTICATION:
			break;
			
		case UNKNOWN_KEYWORD:
			break;
		}
	}

}
