package org.torproject.jtor.directory.impl.certificate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.torproject.jtor.TorParsingException;
import org.torproject.jtor.crypto.TorSignature;
import org.torproject.jtor.data.IPv4Address;
import org.torproject.jtor.directory.Directory;
import org.torproject.jtor.directory.KeyCertificate;
import org.torproject.jtor.directory.parsing.DocumentFieldParser;
import org.torproject.jtor.directory.parsing.DocumentParser;
import org.torproject.jtor.directory.parsing.DocumentParsingHandler;

public class KeyCertificateParser implements DocumentParser<KeyCertificate> {
	private final static int CURRENT_CERTIFICATE_VERSION = 3;
	private final DocumentFieldParser fieldParser;
	private final List<KeyCertificate> certificates;
	private KeyCertificateImpl currentCertificate;
	
	public KeyCertificateParser(DocumentFieldParser fieldParser) {
		this.fieldParser = fieldParser;
		this.fieldParser.setHandler(createParsingHandler());
		certificates = new ArrayList<KeyCertificate>();
	}
	
	private DocumentParsingHandler createParsingHandler() {
		return new DocumentParsingHandler() {
			public void parseKeywordLine() {
				processKeywordLine();
			}
			public void endOfDocument() {
				fieldParser.logDebug("Added "+ certificates.size() +" certificates.");				
			}
		};
	}
	
	private void processKeywordLine() {
		final KeyCertificateKeyword keyword = KeyCertificateKeyword.findKeyword(fieldParser.getCurrentKeyword());
		/*
		 * dirspec.txt (1.2)
		 * When interpreting a Document, software MUST ignore any KeywordLine that
		 * starts with a keyword it doesn't recognize;
		 */
		if(!keyword.equals(KeyCertificateKeyword.UNKNOWN_KEYWORD))
			processKeyword(keyword);
	}
	
	private void startNewCertificate() {
		fieldParser.startSignedEntity();
		currentCertificate = new KeyCertificateImpl();
	}
	
	public void parse() {
		startNewCertificate();
		fieldParser.processDocument();
	}
	
	public void parseAndAddToDirectory(Directory directory) {
		parse();
		for(KeyCertificate cert: certificates) {
			directory.addCertificate(cert);
		}
	}
	
	private void processKeyword(KeyCertificateKeyword keyword) {
		switch(keyword) {
		case DIR_KEY_CERTIFICATE_VERSION:
			processCertificateVersion();
			break;
		case DIR_ADDRESS:
			processDirectoryAddress();
			break;
		case FINGERPRINT:
			currentCertificate.setAuthorityFingerprint(fieldParser.parseHexDigest());
			break;
		case DIR_IDENTITY_KEY:
			currentCertificate.setAuthorityIdentityKey(fieldParser.parsePublicKey());
			break;
		case DIR_SIGNING_KEY:
			currentCertificate.setAuthoritySigningKey(fieldParser.parsePublicKey());
			break;
		case DIR_KEY_PUBLISHED:
			currentCertificate.setKeyPublishedTime(fieldParser.parseTimestamp());
			break;
		case DIR_KEY_EXPIRES:
			currentCertificate.setKeyExpiryTime(fieldParser.parseTimestamp());
			break;
		case DIR_KEY_CROSSCERT:
			// XXX
			fieldParser.parseObject();
			break;
		case DIR_KEY_CERTIFICATION:
			processCertificateSignature();
			break;
		}
	}
	
	private void processCertificateVersion() {
		final int version = fieldParser.parseInteger();
		if(version != CURRENT_CERTIFICATE_VERSION)
			throw new TorParsingException("Unexpected certificate version: " + version);
	}
	
	private void processDirectoryAddress() {
		final String addrport = fieldParser.parseString();
		final String[] args = addrport.split(":");
		if(args.length != 2)
			throw new TorParsingException("Address/Port string incorrectly formed: " + addrport);
		currentCertificate.setDirectoryAddress(IPv4Address.createFromString(args[0]));
		currentCertificate.setDirectoryPort(fieldParser.parsePort(args[1]));
	}
	
	private boolean verifyCurrentCertificate(TorSignature signature) {
		if(!fieldParser.verifySignedEntity(currentCertificate.getAuthorityIdentityKey(), signature)) {
			fieldParser.logWarn("Signature failed for certificate with fingerprint: "+ currentCertificate.getAuthorityFingerprint());
			return false;
		}
		currentCertificate.setValidSignature();
		final boolean isValid = currentCertificate.isValidDocument();
		if(!isValid)
			fieldParser.logWarn("Certificate data is invalid for certificate with fingerprint: "+ currentCertificate.getAuthorityFingerprint());
		return isValid;
	}
	
	private void processCertificateSignature() {
		fieldParser.endSignedEntity();
		if(verifyCurrentCertificate(fieldParser.parseSignature()))
			certificates.add(currentCertificate);
		startNewCertificate();
	}

	public List<KeyCertificate> getDocuments() {
		return Collections.unmodifiableList(certificates);
	}
}
