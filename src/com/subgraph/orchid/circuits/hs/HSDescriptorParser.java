package com.subgraph.orchid.circuits.hs;

import com.subgraph.orchid.TorParsingException;
import com.subgraph.orchid.directory.parsing.DocumentFieldParser;
import com.subgraph.orchid.directory.parsing.DocumentParser;
import com.subgraph.orchid.directory.parsing.DocumentParsingResultHandler;

public class HSDescriptorParser implements DocumentParser<HSDescriptor>{

	private final DocumentFieldParser fieldParser;
	private final HSDescriptor descriptor;
	
	public HSDescriptorParser(DocumentFieldParser fieldParser) {
		this.fieldParser = fieldParser;
		this.descriptor = new HSDescriptor();
	}
	
	public boolean parse(DocumentParsingResultHandler<HSDescriptor> resultHandler) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private void processKeyword(HSDescriptorKeyword keyword) {
		switch(keyword) {
		case VERSION:
			if(fieldParser.parseInteger() != 2) {
				throw new TorParsingException("Unexpected Descriptor version");
			}
			break;
			
		case PERMANENT_KEY:
			descriptor.setPermanentKey(fieldParser.parsePublicKey());
			break;
			
		case SECRET_ID_PART:
			
			break;
		case PUBLICATION_TIME:
			fieldParser.parseTimestamp();
			break;
		case PROTOCOL_VERSIONS:
			break;
		case INTRODUCTION_POINTS:
			break;
		case SIGNATURE:
			break;
		case UNKNOWN_KEYWORD:
			break;
		}
	}

}
