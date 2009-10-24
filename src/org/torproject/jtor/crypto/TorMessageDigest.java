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
		try {
			// Make a clone because #digest() will reset the MessageDigest instance
			// and we want to be able to use this class for running digests on circuits
			final MessageDigest clone = (MessageDigest) digestInstance.clone();
			return clone.digest();
		} catch (CloneNotSupportedException e) {
			throw new TorException(e);
		}
	}

	/**
	 * Return what the digest for the current running hash would be IF we
	 * added <code>data</code>, but don't really add the data to the digest
	 * calculation.
	 */
	public byte[] peekDigest(byte[] data, int offset, int length) {
		try {
			final MessageDigest clone = (MessageDigest) digestInstance.clone();
			clone.update(data, offset, length);
			return clone.digest();
		} catch (CloneNotSupportedException e) {
			throw new TorException(e);
		}
	}

	public HexDigest getHexDigest() {
		return HexDigest.createFromDigestBytes(getDigestBytes());
	}

	public void update(byte[] input) {
		digestInstance.update(input);
	}

	public void update(byte[] input, int offset, int length) {
		digestInstance.update(input, offset, length);
	}

	public void update(String input) {
		try {
			digestInstance.update(input.getBytes("ISO-8859-1"));
		} catch (UnsupportedEncodingException e) {
			throw new TorException(e);
		}
	}

}
