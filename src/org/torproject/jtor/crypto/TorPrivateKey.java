package org.torproject.jtor.crypto;

import java.io.IOException;
import java.io.StringReader;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMReader;
import org.torproject.jtor.TorException;
import org.torproject.jtor.TorParsingException;
import org.torproject.jtor.data.HexDigest;

public class TorPrivateKey {
	
	static public TorPrivateKey generateNewKeypair() {
		KeyPairGenerator generator = createGenerator();
		generator.initialize(1024, new SecureRandom());
		KeyPair pair = generator.generateKeyPair();
		return new TorPrivateKey((RSAPrivateKey)pair.getPrivate(), (RSAPublicKey)pair.getPublic());
	}
	
	static KeyPairGenerator createGenerator() {
		try {
			return KeyPairGenerator.getInstance("RSA", "BC");
		} catch (NoSuchAlgorithmException e) {
			throw new TorException(e);
		} catch (NoSuchProviderException e) {
			throw new TorException(e);
		}
	}
	
	static public TorPrivateKey createFromPEMBuffer(String buffer) {
		final PEMReader pemReader = new PEMReader(new StringReader(buffer));
		final KeyPair kp = readPEMKeyPair(pemReader);
		if(kp.getPublic() instanceof RSAPublicKey && kp.getPrivate() instanceof RSAPrivateKey) 
			return new TorPrivateKey((RSAPrivateKey)kp.getPrivate(), (RSAPublicKey)kp.getPublic());
		else
			throw new TorParsingException("Failed to extract PEM private key");
	}

	static private KeyPair readPEMKeyPair(PEMReader reader) {
		try {
			final Object ob = reader.readObject();
			return verifyObjectAsKeyPair(ob);
		} catch (IOException e) {
			throw new TorException(e);
		}
	}

	static private KeyPair verifyObjectAsKeyPair(Object ob) {
		if(ob instanceof KeyPair)
			return ((KeyPair)ob);
		else
			throw new TorParsingException("Failed to extract PEM private key");
	}

	private final TorPublicKey publicKey;
	private final RSAPrivateKey privateKey;
	
	TorPrivateKey(RSAPrivateKey privateKey, RSAPublicKey publicKey) {
		this.privateKey = privateKey;
		this.publicKey = new TorPublicKey(publicKey);
	}
	
	public TorPublicKey getPublicKey() {
		return publicKey;
	}
	
	public RSAPublicKey getRSAPublicKey() {
		return publicKey.getRSAPublicKey();
	}
	
	public RSAPrivateKey getRSAPrivateKey() {
		return privateKey;
	}
	
	private HexDigest keyFingerprint = null;
	
	public HexDigest getFingerPrint() {
		if(keyFingerprint == null)
			keyFingerprint = HexDigest.createDigestForData(toASN1Raw());
		return keyFingerprint;
	}

	private byte[] toASN1Raw() {
		byte[] encoded = privateKey.getEncoded();
		ASN1InputStream asn1input = new ASN1InputStream(encoded);
		try {
			PrivateKeyInfo info = PrivateKeyInfo.getInstance(asn1input.readObject());
			return info.getDEREncoded();
		} catch (IOException e) {
			throw new TorException(e);
		}
	}
}
