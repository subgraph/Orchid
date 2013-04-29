package com.subgraph.orchid.crypto;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.List;

import com.subgraph.orchid.crypto.ASN1Parser.ASN1BitString;
import com.subgraph.orchid.crypto.ASN1Parser.ASN1Integer;
import com.subgraph.orchid.crypto.ASN1Parser.ASN1Object;
import com.subgraph.orchid.crypto.ASN1Parser.ASN1Sequence;
import com.subgraph.orchid.encoders.Base64;

public class RSAKeyEncoder {
	private final static String HEADER = "-----BEGIN RSA PUBLIC KEY-----";
	private final static String FOOTER = "-----END RSA PUBLIC KEY-----";
	
	private final ASN1Parser asn1Parser = new ASN1Parser();
	
	public RSAPublicKey parsePEMPublicKey(String pem) throws GeneralSecurityException {
		try {
			byte[] bs = decodeAsciiArmoredPEM(pem);
			ByteBuffer data = ByteBuffer.wrap(bs);
			final ASN1Object ob = asn1Parser.parseASN1(data);
			final List<ASN1Object> seq = asn1ObjectToSequence(ob, 2);
			final BigInteger modulus = asn1ObjectToBigInt(seq.get(0));
			final BigInteger exponent = asn1ObjectToBigInt(seq.get(1));
			return createKeyFromModulusAndExponent(modulus, exponent);
		} catch (IllegalArgumentException e) {
			throw new InvalidKeyException();
		}
	}

	private RSAPublicKey createKeyFromModulusAndExponent(BigInteger modulus, BigInteger exponent) throws GeneralSecurityException {
		RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
		KeyFactory fac = KeyFactory.getInstance("RSA");
		return (RSAPublicKey) fac.generatePublic(spec);
	}

	public byte[] getRawEncoded(RSAPublicKey publicKey) {
		final byte[] bs = publicKey.getEncoded();
		final ASN1Object ob = asn1Parser.parseASN1(ByteBuffer.wrap(bs));
		final List<ASN1Object> seq = asn1ObjectToSequence(ob, 2);
		return asn1ObjectToBitString(seq.get(1));
	}

	private BigInteger asn1ObjectToBigInt(ASN1Object ob) {
		if(!(ob instanceof ASN1Integer)) {
			throw new IllegalArgumentException();
		}
		final ASN1Integer n = (ASN1Integer) ob;
		return n.getValue();
	}
	

	private List<ASN1Object> asn1ObjectToSequence(ASN1Object ob, int expectedSize) {
		if(ob instanceof ASN1Sequence) {
			final ASN1Sequence seq = (ASN1Sequence) ob;
			if(seq.getItems().size() != expectedSize) {
				throw new IllegalArgumentException();
			}
			return seq.getItems();
		}
		throw new IllegalArgumentException();
	}

	private byte[] asn1ObjectToBitString(ASN1Object ob) {
		if(!(ob instanceof ASN1BitString)) {
			throw new IllegalArgumentException();
		}
		final ASN1BitString bitstring = (ASN1BitString) ob;
		return bitstring.getBytes();
	}

	private byte[] decodeAsciiArmoredPEM(String pem) {
		final String trimmed = removeDelimiters(pem);
		return Base64.decode(trimmed);
	}
	
	private String removeDelimiters(String pem) {
		if(pem.contains(HEADER) && pem.contains(FOOTER)) {
			return pem.replace(HEADER, "").replace(FOOTER, "");
		}
		throw new IllegalArgumentException("...");
	}

}
