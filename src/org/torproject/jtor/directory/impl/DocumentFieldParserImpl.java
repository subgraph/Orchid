package org.torproject.jtor.directory.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import org.bouncycastle.util.encoders.Base64;
import org.torproject.jtor.TorException;
import org.torproject.jtor.TorParsingException;
import org.torproject.jtor.crypto.TorMessageDigest;
import org.torproject.jtor.crypto.TorPublicKey;
import org.torproject.jtor.crypto.TorSignature;
import org.torproject.jtor.data.HexDigest;
import org.torproject.jtor.data.IPv4Address;
import org.torproject.jtor.data.Timestamp;
import org.torproject.jtor.directory.parsing.DocumentFieldParser;
import org.torproject.jtor.directory.parsing.DocumentObject;
import org.torproject.jtor.directory.parsing.DocumentParsingHandler;
import org.torproject.jtor.logging.Logger;

public class DocumentFieldParserImpl implements DocumentFieldParser {
	private final static String BEGIN_TAG = "-----BEGIN";
	private final static String END_TAG = "-----END";
	private final static String TAG_DELIMITER = "-----";
	private final static String DEFAULT_DELIMITER = " ";
	private final BufferedReader reader;
	private final Logger logger;
	private String delimiter = DEFAULT_DELIMITER;
	private String currentKeyword;
	private List<String> currentItems;
	private int currentItemsPosition;
	private boolean recognizeOpt;
	/* If a line begins with this string do not include it in the current signature. */
 	private String signatureIgnoreToken;
	private boolean isProcessingSignedEntity = false;
	private TorMessageDigest signatureDigest;
	private StringBuilder rawDocumentBuffer;

	private DocumentParsingHandler callbackHandler;

	public DocumentFieldParserImpl(InputStream input, Logger logger) {
		try {
			reader = new BufferedReader(new InputStreamReader(input, "ISO-8859-1"));
		} catch (UnsupportedEncodingException e) {
			throw new TorException(e);
		}
		this.logger = logger;
		rawDocumentBuffer = new StringBuilder();
	}

	public DocumentFieldParserImpl(Reader reader, Logger logger) {
		if(reader instanceof BufferedReader) {
			this.reader = (BufferedReader) reader;
		} else {
			this.reader = new BufferedReader(reader);
		}
		this.logger = logger;
		rawDocumentBuffer = new StringBuilder();
	}

	public String parseNickname() {
		// XXX verify valid nickname
		return getItem();
	}
	public String parseString() {
		return getItem();
	}

	public void setRecognizeOpt() {
		recognizeOpt = true;
	}

	public void setHandler(DocumentParsingHandler handler) {
		callbackHandler = handler;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public int argumentsRemaining() {
		return currentItems.size() - currentItemsPosition;
	}

	private String getItem() {
		if(currentItemsPosition >= currentItems.size()) 
			throw new TorParsingException("Overrun while reading arguments");
		return currentItems.get(currentItemsPosition++);
	}
	/*
	 * Return a string containing all remaining arguments concatenated together
	 */
	public String parseConcatenatedString() {
		StringBuilder result = new StringBuilder();
		while(argumentsRemaining() > 0) {
			if(result.length() > 0)
				result.append(" ");
			result.append(getItem());
		}
		return result.toString();
	}

	public boolean parseBoolean() {
		final int i = parseInteger();
		if(i == 1)
			return true;
		else if(i == 0)
			return false;
		else 
			throw new TorParsingException("Illegal boolean value: "+ i);
	}

	public int parseInteger() {
		return parseInteger(getItem());
	}

	public int parseInteger(String item) {
		try {
			return Integer.parseInt(item);
		} catch(NumberFormatException e) {
			throw new TorParsingException("Failed to parse expected integer value: " + item);
		}
	}

	public int parsePort() {
		return parsePort(getItem());
	}

	public int parsePort(String item) {
		final int port = parseInteger(item);
		if(port < 0 || port > 65535)
			throw new TorParsingException("Illegal port value: " + port);
		return port;
	}

	public Timestamp parseTimestamp() {
		return Timestamp.createFromDateAndTimeString(getItem() +" "+ getItem());
	}

	public HexDigest parseHexDigest() {
		return HexDigest.createFromString(parseString());
	}

	public HexDigest parseFingerprint() {
		return HexDigest.createFromString(parseConcatenatedString());
	}

	public void verifyExpectedArgumentCount(String keyword, int argumentCount) {
		verifyExpectedArgumentCount(keyword, argumentCount, argumentCount);
	}

	private  void verifyExpectedArgumentCount(String keyword, int expectedMin, int expectedMax) {
		final int argumentCount = argumentsRemaining();
		if(expectedMin != -1 && argumentCount < expectedMin) 
			throw new TorParsingException("Not enough arguments for keyword '"+ keyword +"' expected "+ expectedMin +" and got "+ argumentCount);

		if(expectedMax != -1 && argumentCount > expectedMax)
			// Is this the correct thing to do, or should just be a warning?
			throw new TorParsingException("Too many arguments for keyword '"+ keyword +"' expected "+ expectedMax +" and got "+ argumentCount);
	}

	public byte[] parseBase64Data() {
		final StringBuilder string = new StringBuilder(getItem());
		switch(string.length() % 4) {
		case 2:
			string.append("==");
			break;
		case 3:
			string.append("=");
		}
		try {
			return Base64.decode(string.toString().getBytes("ISO-8859-1"));
		} catch (UnsupportedEncodingException e) {
			throw new TorException(e);
		}

	}

	public IPv4Address parseAddress() {
		return IPv4Address.createFromString(getItem());
	}

	public TorPublicKey parsePublicKey() {
		final DocumentObject documentObject = parseObject();
		return TorPublicKey.createFromPEMBuffer(documentObject.getContent());
	}

	public TorSignature parseSignature() {
		final DocumentObject documentObject = parseObject();
		TorSignature s = TorSignature.createFromPEMBuffer(documentObject.getContent());
		return s;
	}

	public DocumentObject parseTypedObject(String type) {
		final DocumentObject object = parseObject();
		if(!type.equals(object.getKeyword()))
			throw new TorParsingException("Unexpected object type.  Expecting: "+ type +", but got: "+ object.getKeyword());
		return object;
	}

	public DocumentObject parseObject() {
		final String line = readLine();
		final String keyword = parseObjectHeader(line);
		final DocumentObject object = new DocumentObject(keyword);
		object.addContent(line);
		parseObjectBody(object, keyword);
		return object;
	}

	private String parseObjectHeader(String headerLine) {
		if(!(headerLine.startsWith(BEGIN_TAG) && headerLine.endsWith(TAG_DELIMITER)))
			throw new TorParsingException("Did not find expected object start tag.");
		return headerLine.substring(BEGIN_TAG.length() + 1, 
				headerLine.length() - TAG_DELIMITER.length());
	}

	private void parseObjectBody(DocumentObject object, String keyword) {
		final String endTag = END_TAG +" "+ keyword +TAG_DELIMITER;
		while(true) {
			final String line = readLine();
			if(line == null) {
				throw new TorParsingException("EOF reached before end of '"+ keyword +"' object.");
			}
			if(line.equals(endTag)) {
				object.addContent(line);
				return;
			}
			parseObjectContent(object, line);
		}
	}

	private void parseObjectContent(DocumentObject object, String content) {
		// XXX verify legal base64 data
		object.addContent(content);
	}

	public String getCurrentKeyword() {
		return currentKeyword;
	}

	public void processDocument() {
		if(callbackHandler == null) 
			throw new TorException("DocumentFieldParser#processDocument() called with null callbackHandler");

		while(true) {
			final String line = readLine();
			if(line == null) {
				callbackHandler.endOfDocument();
				return;
			}
			if(processLine(line))
				callbackHandler.parseKeywordLine();
		}
	}

	public void startSignedEntity() {
		isProcessingSignedEntity = true;
		signatureDigest = new TorMessageDigest();
	}

	public void endSignedEntity() {
		isProcessingSignedEntity = false;
	}

	public void setSignatureIgnoreToken(String token) {
		signatureIgnoreToken = token;
	}

	public TorMessageDigest getSignatureMessageDigest() {
		return signatureDigest;
	}

	private void updateRawDocument(String line) {
		rawDocumentBuffer.append(line);
		rawDocumentBuffer.append('\n');
	}

	public String getRawDocument() {
		return rawDocumentBuffer.toString();
	}

	public void resetRawDocument() {
		rawDocumentBuffer = new StringBuilder();
	}

	public boolean verifySignedEntity(TorPublicKey publicKey, TorSignature signature) {
		isProcessingSignedEntity = false;
		return publicKey.verifySignature(signature, signatureDigest);
	}

	private String readLine() {
		try {
			final String line = reader.readLine();
			if(line != null) {
				updateCurrentSignature(line);
				updateRawDocument(line);
			}
			return line;
		} catch (IOException e) {
			throw new TorParsingException("I/O error parsing document: " + e.getMessage(), e);
		}
	}

	private void updateCurrentSignature(String line) {
		if(!isProcessingSignedEntity)
			return;
		if(signatureIgnoreToken != null && line.startsWith(signatureIgnoreToken))
			return;
		signatureDigest.update(line + "\n");
	}

	private boolean processLine(String line) {
		final List<String> lineItems = Arrays.asList(line.split(delimiter));
		if(lineItems.size() == 0 || lineItems.get(0).length() == 0) {
			// XXX warn
			return false;
		}

		currentKeyword = lineItems.get(0);
		currentItems = lineItems;
		currentItemsPosition = 1;

		if(recognizeOpt && currentKeyword.equals("opt") && lineItems.size() > 1) {
			currentKeyword = lineItems.get(1);
			currentItemsPosition = 2;
		}

		return true;
	}

	public void logDebug(String message) {
		logger.debug(message);
	}

	public void logError(String message) {
		logger.error(message);
	}

	public void logWarn(String message) {
		logger.warning(message);
	}

}
