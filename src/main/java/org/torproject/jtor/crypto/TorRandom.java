package org.torproject.jtor.crypto;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.torproject.jtor.TorException;

public class TorRandom {
	
	private final SecureRandom random;
	
	public TorRandom() {
		random = createRandom();
	}
	
	private static SecureRandom createRandom() {
		try {
			return SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			throw new TorException(e);
		}
	}
	
	public int nextInt(int n) {
		return random.nextInt(n);
	}
	
	public int nextInt() {
		return random.nextInt();
	}
	
	public long nextLong() {
		return random.nextLong();
	}

}
