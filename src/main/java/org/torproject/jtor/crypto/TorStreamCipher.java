package org.torproject.jtor.crypto;

import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.torproject.jtor.TorException;

public class TorStreamCipher {
	public static final int KEY_LEN = 16;
	
	public static TorStreamCipher createWithRandomKey() {
		final SecretKey randomKey = generateRandomKey();
		return new TorStreamCipher(randomKey.getEncoded());
	}
	
	public static TorStreamCipher createFromKeyBytes(byte[] keyBytes) {
		return new TorStreamCipher(keyBytes);
	}
	private static final int BLOCK_SIZE = 16;
	private final Cipher cipher;
	private final byte[] counter;
	private final byte[] counterOut;
	/* Next byte of keystream in counterOut */
	private int keystreamPointer = -1;
	private final SecretKeySpec key;
	
	
	private TorStreamCipher(byte[] keyBytes) {
		key = keyBytesToSecretKey(keyBytes);
		cipher = createCipher(key);
		counter = new byte[BLOCK_SIZE];
		counterOut = new byte[BLOCK_SIZE];	
	}
	
	public void encrypt(byte[] data) {
		encrypt(data, 0, data.length);
	}
	
	public synchronized void encrypt(byte[] data, int offset, int length) {
		for(int i = 0; i < length; i++) 
			data[i + offset] ^= nextKeystreamByte();
	}
	
	public byte[] getKeyBytes() {
		return key.getEncoded();
	}
	
	private static SecretKeySpec keyBytesToSecretKey(byte[] keyBytes) {
		return new SecretKeySpec(keyBytes, "AES");
	}
	
	private static Cipher createCipher(SecretKeySpec keySpec) {
		try {
			final Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding", "BC");
			cipher.init(Cipher.ENCRYPT_MODE, keySpec);
			return cipher;
		} catch (GeneralSecurityException e) {
			throw new TorException(e);
		} 
	}
	
	private static SecretKey generateRandomKey() {
		try {
			KeyGenerator generator = KeyGenerator.getInstance("AES", "BC");
			generator.init(128);
			return generator.generateKey();
		} catch (GeneralSecurityException e) {
			throw new TorException(e);
		} 
	}
	
	private byte nextKeystreamByte() {
		if(keystreamPointer == -1 || (keystreamPointer >= BLOCK_SIZE))
			updateCounter();
		return counterOut[keystreamPointer++];
	}
	private void updateCounter() {
		encryptCounter();
		incrementCounter();
		keystreamPointer = 0;
	}
	
	private void encryptCounter() {
		try {
			cipher.doFinal(counter, 0, BLOCK_SIZE, counterOut, 0);
		} catch (GeneralSecurityException e) {
			throw new TorException(e);
		}
	}
	
	private void incrementCounter() {
		int carry = 1;
		for(int i = counter.length - 1; i >= 0; i--) {
			int x = (counter[i] & 0xff) + carry;
			if(x > 0xff) 
				carry = 1;
			else
				carry = 0;
			counter[i] = (byte)x;
		}
	}

}
