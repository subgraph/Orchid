package org.torproject.jtor.directory.impl.status;

import org.torproject.jtor.TorParsingException;
import org.torproject.jtor.data.HexDigest;
import org.torproject.jtor.directory.impl.status.StatusDocumentParser.DocumentSection;
import org.torproject.jtor.directory.parsing.DocumentFieldParser;

public class RouterStatusSectionParser extends StatusDocumentSectionParser {

	private RouterStatusImpl currentEntry = null;
	
	RouterStatusSectionParser(DocumentFieldParser parser, StatusDocumentImpl document) {
		super(parser, document);
	}
	
	@Override
	void parseLine(DocumentKeyword keyword) {
		if(!keyword.equals(DocumentKeyword.R))
			assertCurrentEntry();
		switch(keyword) {
		case R:
			parseFirstLine();
			break;
		case S:
			parseFlags();
			break;
		case V:
			parseVersion();
			break;
		case W:
			parseBandwidth();
			break;
		case P:
			parsePortList();
			break;
		}
	}

	private void assertCurrentEntry() {
		if(currentEntry == null) 
			throw new TorParsingException("Router status entry must begin with an 'r' line");
	}
	
	private void addCurrentEntry() {
		assert(currentEntry != null);
		if(currentEntry != null)
			document.addRouterStatusEntry(currentEntry);
		currentEntry = null;
	}
	
	private void parseFirstLine() {
		if(currentEntry != null)
			throw new TorParsingException("Unterminated router status entry.");
		currentEntry = new RouterStatusImpl();
		currentEntry.setNickname(fieldParser.parseString());
		currentEntry.setIdentity(parseBase64Digest());
		currentEntry.setDigest(parseBase64Digest());
		currentEntry.setPublicationTime(fieldParser.parseTimestamp());
		currentEntry.setAddress(fieldParser.parseAddress());
		currentEntry.setRouterPort(fieldParser.parsePort());
		currentEntry.setDirectoryPort(fieldParser.parsePort());
	}
	
	private HexDigest parseBase64Digest() {
		return HexDigest.createFromDigestBytes(fieldParser.parseBase64Data());
	}
	
	private void parseFlags() {
		while(fieldParser.argumentsRemaining() > 0)
			currentEntry.addFlag(fieldParser.parseString());
	}
	
	private void parseVersion() {
		currentEntry.setVersion(fieldParser.parseConcatenatedString());
	}
	
	private void parseBandwidth() {
		while(fieldParser.argumentsRemaining() > 0) {
			final String[] parts = fieldParser.parseString().split("=");
			if(parts.length == 2)
				parseBandwidthItem(parts[0], fieldParser.parseInteger(parts[1]));
		}
	}
	
	private void parseBandwidthItem(String key, int value) {
		if(key.equals("Bandwidth")) 
			currentEntry.setEstimatedBandwidth(value);
		else if(key.equals("Measured"))
			currentEntry.setMeasuredBandwidth(value);
	}
	
	private void parsePortList() {
		final String arg = fieldParser.parseString();
		if(arg.equals("accept")) {
			currentEntry.setAcceptedPorts(fieldParser.parseString());
		} else if(arg.equals("reject")) {
			currentEntry.setRejectedPorts(fieldParser.parseString());
		}
		addCurrentEntry();
	}
	
	@Override
	String getNextStateKeyword() {
		return "directory-signature";
	}

	@Override
	DocumentSection getSection() {
		return DocumentSection.ROUTER_STATUS;
	}
	
	DocumentSection nextSection() {
		return DocumentSection.SIGNATURE;
	}

}
