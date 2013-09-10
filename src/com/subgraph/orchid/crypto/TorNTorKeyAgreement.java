package com.subgraph.orchid.crypto;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.subgraph.orchid.data.HexDigest;

public class TorNTorKeyAgreement implements TorKeyAgreement {
	public final static int CURVE25519_PUBKEY_LEN = 32;
	final static int CURVE25519_OUTPUT_LEN = 32;
	final static int DIGEST256_LEN = 32;
	final static int DIGEST_LEN = 20;
	final static int KEY_LEN = 16;
	final static int NTOR_ONIONSKIN_LEN = 2 * CURVE25519_PUBKEY_LEN + DIGEST_LEN;
	final static String PROTOID = "ntor-curve25519-sha256-1";
	final static String SERVER_STR = "Server";
	final static int SECRET_INPUT_LEN = CURVE25519_PUBKEY_LEN * 3 + CURVE25519_OUTPUT_LEN * 2 + DIGEST_LEN + PROTOID.length();
	final static int AUTH_INPUT_LEN = DIGEST256_LEN + DIGEST_LEN + (CURVE25519_PUBKEY_LEN * 3) + PROTOID.length()  + SERVER_STR.length(); 
	final static Charset cs = Charset.forName("ISO-8859-1");
	
	private final TorRandom random = new TorRandom();
	private final HexDigest peerIdentity;
	private final byte[] peerNTorOnionKey;  /* pubkey_B */
	private final byte[] secretKey_x; 
	private final byte[] publicKey_X; 

	public TorNTorKeyAgreement(HexDigest peerIdentity, byte[] peerNTorOnionKey) {
		this.peerIdentity = peerIdentity;
		this.peerNTorOnionKey = peerNTorOnionKey;
		this.secretKey_x = generateSecretKey();
		this.publicKey_X = getPublicKeyForPrivate(secretKey_x);
	}
	
	
	public byte[] createOnionSkin() {
		final ByteBuffer buffer = makeBuffer(NTOR_ONIONSKIN_LEN);
		buffer.put(peerIdentity.getRawBytes());
		buffer.put(peerNTorOnionKey);
		buffer.put(publicKey_X);
		return buffer.array();
	}

	private ByteBuffer makeBuffer(int sz) {
		final byte[] array = new byte[sz];
		return ByteBuffer.wrap(array);
	}
	
	byte[] generateSecretKey() {
		final byte[]key = random.getBytes(32);
		key[0] &= 248;
		key[31] &= 127;
		key[31] |= 64;
		return key;
	}
	
	byte[] getPublicKeyForPrivate(byte[] secretKey) {
		final byte[] pub = new byte[32];
		Curve25519.crypto_scalarmult_base(pub, secretKey);
		return pub;
	}
	
	private boolean isBad;

	public boolean deriveKeysFromHandshakeResponse(byte[] handshakeResponse, byte[] keyMaterialOut, byte[] verifyHashOut) {
		isBad = false;
				
		final ByteBuffer hr = ByteBuffer.wrap(handshakeResponse);
		byte[] serverPub = new byte[CURVE25519_PUBKEY_LEN];
		byte[] authCandidate = new byte[DIGEST256_LEN];
		hr.get(serverPub);
		hr.get(authCandidate);

		final byte[] secretInput = buildSecretInput(serverPub);
		final byte[] verify = tweak("verify", secretInput);
		final byte[] authInput = buildAuthInput(verify, serverPub);
		final byte[] auth = tweak("mac", authInput);
		isBad |= !constantTimeArrayEquals(auth, authCandidate);
		final byte[] seed = tweak("key_extract", secretInput);
		
		final int keyBytesLength = keyMaterialOut.length + verifyHashOut.length;
		final byte[] keyBytes = expandKey(seed, keyBytesLength);
		System.arraycopy(keyBytes, 0, keyMaterialOut, 0, keyMaterialOut.length);
		System.arraycopy(keyBytes, keyMaterialOut.length, verifyHashOut, 0, verifyHashOut.length);
		
		return !isBad;

	}
	
	public byte[] getNtorCreateMagic() {
		return "ntorNTORntorNTOR".getBytes(cs);
	}

	private byte[] buildSecretInput(byte[] serverPublic_Y) {
		final ByteBuffer bb = makeBuffer(SECRET_INPUT_LEN);
		bb.put(scalarMult(serverPublic_Y));
		bb.put(scalarMult(peerNTorOnionKey));
		bb.put(peerIdentity.getRawBytes());
		bb.put(peerNTorOnionKey);
		bb.put(publicKey_X);
		bb.put(serverPublic_Y);
		bb.put(PROTOID.getBytes());
		return bb.array();
	}
	
	private byte[] buildAuthInput(byte[] verify, byte[] serverPublic_Y) {
		final ByteBuffer bb = makeBuffer(AUTH_INPUT_LEN);
		bb.put(verify);
		bb.put(peerIdentity.getRawBytes());
		bb.put(peerNTorOnionKey);
		bb.put(serverPublic_Y);
		bb.put(publicKey_X);
		bb.put(PROTOID.getBytes(cs));
		bb.put(SERVER_STR.getBytes(cs));
		return bb.array();
	}
	
	private byte[] scalarMult(byte[] peerValue) {
		final byte[] out = new byte[CURVE25519_OUTPUT_LEN];
		Curve25519.crypto_scalarmult(out, secretKey_x, peerValue);
		isBad |= isAllZero(out);
		return out;
	}
	
	boolean isAllZero(byte[] bs) {
		boolean result = true;
		for(byte b: bs) {
			result &= (b == 0);
		}
		return result;
	}
	
	// XXX should be in util class
	private boolean constantTimeArrayEquals(byte[] a1, byte[] a2) {
		if(a1.length != a2.length)
			return false;
		int result = 0;
		for(int i = 0; i < a1.length; i++)
			result += (a1[i] & 0xFF) ^ (a2[i] & 0xFF);
		return result == 0;
		
	}
	byte[] tweak(String suffix, byte[] input) {
		return hmac256(input, getStringConstant(suffix));
	}
	
	byte[] expandKey(byte[] seed, int len) {
		ByteBuffer out = makeBuffer(len);
		int i = 1;
		byte[] mac = null;
		while(out.hasRemaining()) {
			mac = getKeyExpansionMac(i, mac, seed);
			if(out.remaining() >= DIGEST256_LEN) {
				out.put(mac);
			} else {
				out.put(mac, 0, out.remaining());
			}
			i += 1;
		}
		return out.array();
	}
	
	private byte[] getKeyExpansionMac(int round, byte[] priorMac, byte[] key) {
		return hmac256(getKeyExpansionMacInput(round, priorMac), key);
	}
	
	private byte[] getKeyExpansionMacInput(int round, byte[] priorMac) {
		final byte[] expand = getStringConstant("key_expand");
		final ByteBuffer bb;
		if(round == 1) {
			bb = makeBuffer(expand.length + 1);
		} else {
			bb = makeBuffer(expand.length + DIGEST256_LEN + 1);
			bb.put(priorMac);
		}
		bb.put(expand);
		bb.put((byte) round);
		return bb.array();
	}
	
	byte[] hmac256(byte[] input, byte[] key) {
		final SecretKeySpec keyspec = new SecretKeySpec(key, "HmacSHA256");
		try {
			final Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(keyspec);
			return mac.doFinal(input);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	byte[] getStringConstant(String suffix) {
		if(suffix == null || suffix.isEmpty()) {
			return PROTOID.getBytes(cs);
		} else {
			return (PROTOID + ":" + suffix).getBytes(cs);
		}
	}

}
