package org.torproject.jtor.directory.impl.consensus;

import org.torproject.jtor.crypto.TorMessageDigest;
import org.torproject.jtor.crypto.TorSignature;
import org.torproject.jtor.data.HexDigest;
import org.torproject.jtor.directory.impl.consensus.ConsensusDocumentParser.DocumentSection;
import org.torproject.jtor.directory.parsing.DocumentFieldParser;
import org.torproject.jtor.directory.parsing.NameIntegerParameter;

public class FooterSectionParser extends ConsensusDocumentSectionParser {

	private boolean seenFirstSignature = false;
	
	FooterSectionParser(DocumentFieldParser parser, ConsensusDocumentImpl document) {
		super(parser, document);
	}

	@Override
	String getNextStateKeyword() {
		return null;
	}

	@Override
	DocumentSection getSection() {
		return DocumentSection.FOOTER;
	}
	
	DocumentSection nextSection() {
		return DocumentSection.NO_SECTION;
	}

	@Override
	void parseLine(DocumentKeyword keyword) {
		switch(keyword) {
		case BANDWIDTH_WEIGHTS:
			processBandwidthWeights();
			break;
			
		case DIRECTORY_SIGNATURE:
			processSignature();
			break;

		default:
			break;
		}
	}

	private void doFirstSignature() {
		seenFirstSignature = true;
		fieldParser.endSignedEntity();
		final TorMessageDigest messageDigest = fieldParser.getSignatureMessageDigest();
		messageDigest.update("directory-signature ");
		document.setSigningHash(messageDigest.getHexDigest());
	}
	
	private void processSignature() {
		if(!seenFirstSignature) {
			doFirstSignature();
		}
		HexDigest identity = fieldParser.parseHexDigest();
		HexDigest signingKey = fieldParser.parseHexDigest();
		TorSignature signature = fieldParser.parseSignature();
		document.addSignature(new DirectorySignature(identity, signingKey, signature));
	}
	
	private void processBandwidthWeights() {
		final int remaining = fieldParser.argumentsRemaining();
		for(int i = 0; i < remaining; i++) {
			NameIntegerParameter p = fieldParser.parseParameter();
			document.addBandwidthWeight(p.getName(), p.getValue());
		}
	}
}
