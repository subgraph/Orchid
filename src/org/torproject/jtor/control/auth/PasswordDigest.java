// Copyright 2005 Nick Mathewson, Roger Dingledine
// See LICENSE file for copying information
package org.torproject.jtor.control.auth;

import java.security.SecureRandom;
import java.security.MessageDigest;

/**
 * A hashed digest of a secret password (used to set control connection
 * security.)
 *
 * For the actual hashing algorithm, see RFC2440's secret-to-key conversion.
 */
public class PasswordDigest {

    byte[] secret;
    String hashedKey;

    /** Return a new password digest with a random secret and salt. */
    public static PasswordDigest generateDigest() {
        byte[] secret = new byte[20];
        SecureRandom rng = new SecureRandom();
        rng.nextBytes(secret);
        return new PasswordDigest(secret);
    }

    /** Construct a new password digest with a given secret as it may appear in torrc */
    public PasswordDigest(String in) {
        byte[] specifier;
        if (in.startsWith("16:")) {
            hashedKey = in;
        } else {
            secret = removeQuotes(in).getBytes();
            specifier = new byte[9];
            SecureRandom rng = new SecureRandom();
            rng.nextBytes(specifier);
            specifier[8] = 96;
            hashedKey = "16:"+encodeBytes(secretToKey(secret, specifier));
        }
    }

    /** Construct a new password digest with a given secret and random salt */
    public PasswordDigest(byte[] secret) {
        this(secret, null);
    }

    /** Construct a new password digest with a given secret and random salt.
     * Note that the 9th byte of the specifier determines the number of hash
     * iterations as in RFC2440.
     */
    public PasswordDigest(byte[] secret, byte[] specifier) {
        this.secret = secret.clone();
        if (specifier == null) {
            specifier = new byte[9];
            SecureRandom rng = new SecureRandom();
            rng.nextBytes(specifier);
            specifier[8] = 96;
        }
        hashedKey = "16:"+encodeBytes(secretToKey(secret, specifier));
    }

    /** Return the secret used to generate this password hash.
     */
    public byte[] getSecret() {
        return secret.clone();
    }

    /** Return the hashed password in the format used by Tor. */
    public String getHashedPassword() {
        return hashedKey;
    }

    /** Verifies if a key matches the set password, may be plain text or hashed. */
    public boolean verifyPassword(String in) {
        if (in.startsWith("16:")) { // hashed password
        	if (secret == null) { // can't authenticate this without the secret
        		return false;
        	}
        	byte[] salt = hexStringToByteArray(in.substring(3, 21));
        	PasswordDigest pwd = new PasswordDigest(secret, salt);
        	return pwd.getHashedPassword().equals(in);
        	
        } else if (in.startsWith("\"") && in.endsWith("\"")) { // plain text password
        	byte[] salt = hexStringToByteArray(hashedKey.substring(3, 21));
        	byte[] key = in.substring(1, in.length()-1).getBytes();
            PasswordDigest pwd = new PasswordDigest(key, salt);
            return hashedKey.equals(pwd.getHashedPassword());
        	
        } else { // hex encoded string
            byte[] salt = hexStringToByteArray(hashedKey.substring(3, 21));
            byte[] key = hexStringToByteArray(in);
            PasswordDigest pwd = new PasswordDigest(key, salt);
            return hashedKey.equals(pwd.getHashedPassword());
        }
    }

    /** Parameter used by RFC2440's s2k algorithm. */
    private static final int EXPBIAS = 6;

    /** Implement rfc2440 s2k */
    public static byte[] secretToKey(byte[] secret, byte[] specifier) {
        MessageDigest d;
        try {
            d = MessageDigest.getInstance("SHA-1");
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new RuntimeException("Can't run without sha-1.");
        }
        int c = (specifier[8])&0xff;
        int count = (16 + (c&15)) << ((c>>4) + EXPBIAS);

        byte[] tmp = new byte[8+secret.length];
        System.arraycopy(specifier, 0, tmp, 0, 8);
        System.arraycopy(secret, 0, tmp, 8, secret.length);
        while (count > 0) {
            if (count >= tmp.length) {
                d.update(tmp);
                count -= tmp.length;
            } else {
                d.update(tmp, 0, count);
                count = 0;
            }
        }
        byte[] key = new byte[20+9];
        System.arraycopy(d.digest(), 0, key, 9, 20);
        System.arraycopy(specifier, 0, key, 0, 9);
        return key;
    }

    /** Return a hexadecimal encoding of a byte array. */
    // XXX There must be a better way to do this in Java.
    private static final String encodeBytes(byte[] ba) {
        char[] NYBBLES = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
        };
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < ba.length; ++i) {
            int b = (ba[i]) & 0xff;
            buf.append(NYBBLES[b >> 4]);
            buf.append(NYBBLES[b&0x0f]);
        }
        return buf.toString();
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                             + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    
    /** Removes any unescaped quotes from a given string */
	private String removeQuotes(String in) {
		int index = in.indexOf("\"");
		while (index < in.length() && index > 0) {
			if (!in.substring(index-1, index).equals("\\")) {
				//remove the quote as it's not escaped
				in = in.substring(0, index) + in.substring(index+1);
			}
			index = in.indexOf("\"", index);
		}
		return in;
	}


}

