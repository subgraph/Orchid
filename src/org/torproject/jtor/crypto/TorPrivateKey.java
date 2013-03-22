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
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
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

	static public TorPrivateKey createFromPEMBuffer(String buffer) throws IOException {
		final PEMParser parser = new PEMParser(new StringReader(buffer));
		try {
			JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
			KeyPair kp = converter.getKeyPair((PEMKeyPair) parser.readObject());

			if(kp.getPublic() instanceof RSAPublicKey && kp.getPrivate() instanceof RSAPrivateKey) 
				return new TorPrivateKey((RSAPrivateKey)kp.getPrivate(), (RSAPublicKey)kp.getPublic());
			else
				throw new TorParsingException("Failed to extract PEM private key");
		} finally {
			parser.close();
		}
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

	public HexDigest getFingerprint() {
		if(keyFingerprint == null)
			keyFingerprint = HexDigest.createDigestForData(toASN1Raw());
		return keyFingerprint;
	}

	private byte[] toASN1Raw() {
		byte[] encoded = privateKey.getEncoded();
		ASN1InputStream asn1input = new ASN1InputStream(encoded);
		try {
			PrivateKeyInfo info = PrivateKeyInfo.getInstance(asn1input.readObject());
			return info.getEncoded();
		} catch (IOException e) {
			throw new TorException(e);
		} finally {
			try {
				asn1input.close();
			} catch (IOException e) {
			}
		}
	}
}
