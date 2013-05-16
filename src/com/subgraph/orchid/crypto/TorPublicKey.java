package com.subgraph.orchid.crypto;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.subgraph.orchid.TorException;
import com.subgraph.orchid.data.HexDigest;

/**
 * This class wraps the RSA public keys used in the Tor protocol.
 */
public class TorPublicKey {
	static public TorPublicKey createFromPEMBuffer(String buffer) throws GeneralSecurityException {
		final RSAKeyEncoder encoder = new RSAKeyEncoder();
		RSAPublicKey pkey = encoder.parsePEMPublicKey(buffer);
		return new TorPublicKey(pkey);
	}
	
	private final RSAPublicKey key;
	private HexDigest keyFingerprint = null;

	public TorPublicKey(RSAPublicKey key) {
		this.key = key;
	}

	public HexDigest getFingerprint() {
		if(keyFingerprint == null) {
			final RSAKeyEncoder encoder = new RSAKeyEncoder();
			keyFingerprint = HexDigest.createDigestForData(encoder.getPKCS1Encoded(key));
		}
		return keyFingerprint;
	}

	public boolean verifySignature(TorSignature signature, HexDigest digest) {
		return verifySignatureFromDigestBytes(signature, digest.getRawBytes());
	}

	public boolean verifySignature(TorSignature signature, TorMessageDigest digest) {
		return verifySignatureFromDigestBytes(signature, digest.getDigestBytes());
	}

	public boolean verifySignatureFromDigestBytes(TorSignature signature, byte[] digestBytes) {
		final Cipher cipher = createCipherInstance();
		try {
			byte[] decrypted = cipher.doFinal(signature.getSignatureBytes());
			return constantTimeArrayEquals(decrypted, digestBytes);
		} catch (IllegalBlockSizeException e) {
			throw new TorException(e);
		} catch (BadPaddingException e) {
			throw new TorException(e);
		}
	}

	private boolean constantTimeArrayEquals(byte[] a1, byte[] a2) {
		if(a1.length != a2.length)
			return false;
		int result = 0;
		for(int i = 0; i < a1.length; i++)
			result += (a1[i] & 0xFF) ^ (a2[i] & 0xFF);
		return result == 0;
		
	}

	private Cipher createCipherInstance() {
		try {
			final Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.DECRYPT_MODE, key);
			return cipher;
		} catch (NoSuchAlgorithmException e) {
			throw new TorException(e);
		} catch (NoSuchPaddingException e) {
			throw new TorException(e);
		} catch (InvalidKeyException e) {
			throw new TorException(e);
		}
	}

	public RSAPublicKey getRSAPublicKey() {
		return key;
	}

	public String toString() {
		return "Tor Public Key: " + getFingerprint();
	}

	public boolean equals(Object o) {
		if(!(o instanceof TorPublicKey))
			return false;
		final TorPublicKey other = (TorPublicKey) o;
		return other.getFingerprint().equals(getFingerprint());
	}

	public int hashCode() {
		return getFingerprint().hashCode();
	}
}
