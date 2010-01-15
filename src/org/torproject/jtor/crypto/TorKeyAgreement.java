package org.torproject.jtor.crypto;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;

import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;

import org.torproject.jtor.TorException;
/**
 * The <code>TorKeyAgreement</code> class implements the diffie-hellman key agreement
 * protocol using the parameters specified in the main Tor specification (tor-spec.txt).
 * 
 * An instance of this class can only be used to perform a single key agreement operation.
 * 
 * After instantiating the class, a user calls {@link #getPublicValue()} or {@link #getPublicKeyBytes()}
 * to retrieve the public value to transmit to the peer in the key agreement operation.  After receiving
 * a public value from the peer, this value should be converted into a <code>BigInteger</code> and
 * {@link #isValidPublicValue(BigInteger)} should be called to verify that the peer has sent a safe
 * and legal public value.  If {@link #isValidPublicValue(BigInteger)} returns true, the peer public
 * value is valid and {@link #getSharedSecret(BigInteger)} can be called to complete the key agreement
 * protocol and return the shared secret value.
 * 
 */
public class TorKeyAgreement {
	public final static int DH_LEN = 128;
	public final static int DH_SEC_LEN = 40;
	/*
	 * tor-spec 0.3
	 * 
	 * For Diffie-Hellman, we use a generator (g) of 2.  For the modulus (p), we
     * use the 1024-bit safe prime from rfc2409 section 6.2 whose hex
     * representation is:
	 */
	private static final BigInteger P1024 = new BigInteger(
	  "00FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E08"
    + "8A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B"
    + "302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9"
    + "A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F24117C4B1FE6"
    + "49286651ECE65381FFFFFFFFFFFFFFFF", 16);
	private static final BigInteger G = new BigInteger("2");
	
	/*
	 * tor-spec 0.3
	 * 
	 * As an optimization, implementations SHOULD choose DH private keys (x) of
     * 320 bits.
	 */
	private static final int PRIVATE_KEY_SIZE = 320;
	private static final DHParameterSpec DH_PARAMETER_SPEC = new DHParameterSpec(P1024, G, PRIVATE_KEY_SIZE);
	
	private final KeyAgreement dh;
	private final KeyPair keyPair;
	
	/**
	 * Create a new <code>TorKeyAgreement</code> instance which can be used to perform a single
	 * key agreement operation.  A new set of ephemeral Diffie-Hellman parameters are generated
	 * when this class is instantiated.
	 */
	public TorKeyAgreement() {
		keyPair = generateKeyPair();
		dh = createDH();
	}
	
	/**
	 * Return the generated public value for this key agreement operation as a <code>BigInteger</code>.
	 * 
	 * @return The diffie-hellman public value as a <code>BigInteger</code>.
	 */
	public BigInteger getPublicValue() {
		DHPublicKey pubKey = (DHPublicKey) keyPair.getPublic();
		return pubKey.getY();
	}
	
	/**
	 * Return the generated public value for this key agreement operation as an array with the value
	 * encoded in big-endian byte order.
	 * 
	 * @return A byte array containing the encoded public value for this key agreement operation.
	 */
	public byte[] getPublicKeyBytes() {
		final byte[] output = new byte[128];
		final byte[] yBytes = getPublicValue().toByteArray();
		final int offset = yBytes.length - DH_LEN;
		System.arraycopy(yBytes, offset, output, 0, DH_LEN);
		return output;
	}
	
	/**
	 * Return <code>true</code> if the specified value is a legal public
	 * value rather than a dangerous degenerate or confined subgroup value.  
	 * 
	 * tor-spec 5.2
	 * Before computing g^xy, both client and server MUST verify that 
	 * the received g^x or g^y value is not degenerate; that is, it must
	 * be strictly greater than 1 and strictly less than p-1 where p is 
	 * the DH modulus.  Implementations MUST NOT complete a handshake 
	 * with degenerate keys.
	 */
	public static boolean isValidPublicValue(BigInteger publicValue) {
		if(publicValue.signum() < 1 || publicValue.equals(BigInteger.ONE))
			return false;
		if(publicValue.compareTo(P1024.subtract(BigInteger.ONE)) >= 0)
			return false;
		return true;
	}

	/**
	 * Complete the key agreement protocol with the peer public value
	 * <code>otherPublic</code> and return the calculated shared secret.
	 * 
	 * @param otherPublic The peer public value.
	 * @return The shared secret value produced by the protocol.
	 */
	public byte[] getSharedSecret(BigInteger otherPublic) {
		try {
			KeyFactory factory = KeyFactory.getInstance("DH", "BC");
			DHPublicKeySpec pub = new DHPublicKeySpec(otherPublic, P1024, G);
			PublicKey key = factory.generatePublic(pub);
			dh.doPhase(key, true);
			return dh.generateSecret();
		} catch (GeneralSecurityException e) {
			throw new TorException(e);
		} 
	}
	private final KeyAgreement createDH() {
		try {
			KeyAgreement dh = KeyAgreement.getInstance("DH", "BC");
			dh.init(keyPair.getPrivate());
			return dh;
		} catch (GeneralSecurityException e) {
			throw new TorException(e);
		} 
	}
	
	private final KeyPair generateKeyPair() {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH", "BC");
			keyGen.initialize(DH_PARAMETER_SPEC);
			return keyGen.generateKeyPair();	
		} catch (GeneralSecurityException e) {
			throw new TorException(e);
		} 
	}
}
