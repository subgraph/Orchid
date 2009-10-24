package org.torproject.jtor.data;

import java.util.Arrays;
import java.util.List;

import org.bouncycastle.util.encoders.Hex;
import org.torproject.jtor.TorException;
import org.torproject.jtor.crypto.TorMessageDigest;

/**
 * This class represents both digests and fingerprints that appear in directory
 * documents.  The names fingerprint and digest are used interchangeably in 
 * the specification but generally a fingerprint is a message digest (ie: SHA1)
 * over the DER ASN.1 encoding of a public key.  A digest is usually
 * a message digest over a set of fields in a directory document.
 * 
 * Digests always appear as a 40 character hex string:
 * 
 * 0EA20CAA3CE696E561BC08B15E00106700E8F682
 *
 * Fingerprints may either appear as a single hex string as above or sometimes in
 * a more easily human-parsed spaced format:
 * 
 * 1E0F 5874 2268 E82F C600 D81D 9064 07C5 7CC2 C3A7
 *
 */
public class HexDigest {
	public static HexDigest createFromStringList(List<String> strings) {
		StringBuilder builder = new StringBuilder();
		for(String chunk: strings) 
			builder.append(chunk);
		return createFromString(builder.toString());
	}

	public static HexDigest createFromString(String fingerprint) {
		final String[] parts = fingerprint.split(" ");
		if(parts.length > 1)
			return createFromStringList(Arrays.asList(parts));
		final byte[] digestData = Hex.decode(fingerprint);
		return new HexDigest(digestData);
	}

	public static HexDigest createFromDigestBytes(byte[] data) {
		return new HexDigest(data);
	}
	
	public static HexDigest createDigestForData(byte[] data) {
		final TorMessageDigest digest = new TorMessageDigest();
		digest.update(data);
		return new HexDigest(digest.getDigestBytes());
	}

	private final byte[] digestBytes = new byte[TorMessageDigest.TOR_DIGEST_SIZE];

	private HexDigest(byte[] data) {
		if(data.length != TorMessageDigest.TOR_DIGEST_SIZE) {
			throw new TorException("Digest data is not the correct length "+ data.length +" != " + TorMessageDigest.TOR_DIGEST_SIZE);
		}
		System.arraycopy(data, 0, digestBytes, 0, TorMessageDigest.TOR_DIGEST_SIZE);
	}

	public byte[] getRawBytes() {
		return digestBytes;
	}

	public String toString() {
		return new String(Hex.encode(digestBytes));
	}

	/**
	 * Return a spaced fingerprint representation of this HexDigest. 
	 * 
	 * ex:
	 * 
	 * 1E0F 5874 2268 E82F C600 D81D 9064 07C5 7CC2 C3A7
	 *
	 * @return A string representation of this HexDigest in the spaced fingerprint format.
	 */
	public String toSpacedString() {
		final String original = toString();
		final StringBuilder builder = new StringBuilder();
		for(int i = 0; i < original.length(); i++) {
			if(i > 0 && (i % 4) == 0)
				builder.append(' ');
			builder.append(original.charAt(i));
		}
		return builder.toString();
	}

	public boolean equals(Object o) {
		if(!(o instanceof HexDigest))
			return false;
		final HexDigest other = (HexDigest)o;
		return Arrays.equals(other.digestBytes, this.digestBytes);
	}

	public int hashCode() {
		int hash = 0;
		for(int i = 0; i < 4; i++) {
			hash <<= 8;
			hash |= (digestBytes[i] & 0xFF);
		}
		return hash;
	}

}
