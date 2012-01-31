package org.torproject.jtor.directory.impl.consensus;

import org.torproject.jtor.crypto.TorMessageDigest;
import org.torproject.jtor.crypto.TorSignature;
import org.torproject.jtor.data.HexDigest;
import org.torproject.jtor.directory.impl.consensus.ConsensusDocumentParser.DocumentSection;
import org.torproject.jtor.directory.parsing.DocumentFieldParser;

public class SignatureSectionParser extends ConsensusDocumentSectionParser {

	private boolean seenFirstLine = false;
	
	SignatureSectionParser(DocumentFieldParser parser, ConsensusDocumentImpl document) {
		super(parser, document);
	}

	@Override
	String getNextStateKeyword() {
		return null;
	}

	@Override
	DocumentSection getSection() {
		return DocumentSection.SIGNATURE;
	}
	
	DocumentSection nextSection() {
		return DocumentSection.NO_SECTION;
	}

	@Override
	void parseLine(DocumentKeyword keyword) {
		if(!seenFirstLine)
			doFirstLine();
		switch(keyword) {
		case DIRECTORY_SIGNATURE:
			processSignature();
		}		
	}
	
	private void doFirstLine() {
		seenFirstLine = true;
		fieldParser.endSignedEntity();
		final TorMessageDigest messageDigest = fieldParser.getSignatureMessageDigest();
		messageDigest.update("directory-signature ");
		document.setSigningHash(messageDigest.getHexDigest());
	}
	
	private void processSignature() {
		HexDigest identity = fieldParser.parseHexDigest();
		HexDigest signingKey = fieldParser.parseHexDigest();
		TorSignature signature = fieldParser.parseSignature();
		document.addSignature(new DirectorySignature(identity, signingKey, signature));
	}

}
