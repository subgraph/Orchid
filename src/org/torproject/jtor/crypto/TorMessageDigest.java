package org.torproject.jtor.crypto;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import org.torproject.jtor.TorException;
import org.torproject.jtor.data.HexDigest;

/**
 * This class wraps the default cryptographic message digest algorithm
 * used in Tor (SHA-1). 
 */
public class TorMessageDigest {
	
	public static final int TOR_DIGEST_SIZE = 20;
	private static final String TOR_DIGEST_ALGORITHM = "SHA-1";
	
	private final MessageDigest digestInstance;
	private byte[] digestBytes;
	
	public TorMessageDigest() {
		digestInstance = createDigestInstance();
	}
	
	private MessageDigest createDigestInstance() {
		try {
			return MessageDigest.getInstance(TOR_DIGEST_ALGORITHM, "BC");
		} catch (NoSuchAlgorithmException e) {
			throw new TorException(e);
		} catch (NoSuchProviderException e) {
			throw new TorException(e);
		}
	}
	
	public byte[] getDigestBytes() {
		if(digestBytes == null) 
			digestBytes = digestInstance.digest();
		return digestBytes;
	}
	
	public HexDigest getHexDigest() {
		return HexDigest.createFromDigestBytes(getDigestBytes());
	}
	
	public void update(byte[] input) {
		digestInstance.update(input);
	}
	
	public void update(String input) {
		try {
			digestInstance.update(input.getBytes("ISO-8859-1"));
		} catch (UnsupportedEncodingException e) {
			throw new TorException(e);
		}
	}
	

}
