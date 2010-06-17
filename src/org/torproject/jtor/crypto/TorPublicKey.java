package org.torproject.jtor.crypto;

import java.io.IOException;
import java.io.StringReader;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.interfaces.RSAPublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.PEMReader;
import org.torproject.jtor.TorException;
import org.torproject.jtor.TorParsingException;
import org.torproject.jtor.data.HexDigest;

/**
 * This class wraps the RSA public keys used in the Tor protocol.
 */
public class TorPublicKey {
	static public TorPublicKey createFromPEMBuffer(String buffer) {
		final PEMReader pemReader = new PEMReader( new StringReader(buffer));
		return new TorPublicKey(readPEMPublicKey(pemReader));
	}

	static private RSAPublicKey readPEMPublicKey(PEMReader reader) {
		try {
			final Object ob = reader.readObject();
			return verifyObjectAsKey(ob);
		} catch (IOException e) {
			throw new TorException(e);
		}
	}

	static private RSAPublicKey verifyObjectAsKey(Object ob) {
		if(ob instanceof RSAPublicKey)
			return ((RSAPublicKey) ob);
		else if(ob instanceof KeyPair) {
			KeyPair kp = (KeyPair)ob;
			if(kp.getPublic() instanceof RSAPublicKey)
				return ((RSAPublicKey)kp.getPublic());
		}
		throw new TorParsingException("Failed to extract PEM public key.");
	}

	private final RSAPublicKey key;
	private HexDigest keyFingerprint = null;

	public TorPublicKey(RSAPublicKey key) {
		this.key = key;
	}

	private byte[] toASN1Raw() {
		byte[] encoded = key.getEncoded();
		ASN1InputStream asn1input = new ASN1InputStream(encoded);
		try {
			SubjectPublicKeyInfo info = SubjectPublicKeyInfo.getInstance(asn1input.readObject());
			return info.getPublicKey().getDEREncoded();
		} catch (IOException e) {
			throw new TorException(e);
		}
	}

	public HexDigest getFingerprint() {
		if(keyFingerprint == null)
			keyFingerprint = HexDigest.createDigestForData(toASN1Raw());
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
			final Cipher cipher = Cipher.getInstance("RSA/None/PKCS1Padding", "BC");
			cipher.init(Cipher.DECRYPT_MODE, key);
			return cipher;
		} catch (NoSuchAlgorithmException e) {
			throw new TorException(e);
		} catch (NoSuchProviderException e) {
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
