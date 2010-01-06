package org.torproject.jtor.control.auth;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

import org.torproject.jtor.TorConfig;

public class ControlAuthenticator {

	public static boolean authenticate(TorConfig tc, String input) {
		if (input == null) {
			return false;
		}

		String auth;
		if (input.toLowerCase().startsWith("authenticate"))
			auth = input.substring(13);
		else
			auth = input;

		if (tc.isCookieAuthentication()) {
			try {
				File cookie = new File(tc.getDataDirectory(), "control_auth_cookie");
				BufferedReader reader = new BufferedReader(new FileReader(cookie));
				String magic = reader.readLine();
				reader.close();
				
				auth = new String(PasswordDigest.hexStringToByteArray(auth));
				return magic.equals(auth);
			} catch (Throwable t) {
				return false;
			}
		}
		
		if (tc.get__HashedControlSessionPassword() != null) {
			PasswordDigest cp = new PasswordDigest(tc.get__HashedControlSessionPassword());
			return cp.verifyPassword(auth);
		}

		if (tc.getHashedControlPassword() != null) {
			// generate our control password
			PasswordDigest cp = new PasswordDigest(tc.getHashedControlPassword());
			return cp.verifyPassword(auth);
		}

		return true; // no auth required
	}

	public static boolean writeCookie(TorConfig tc) {
		// write out a cookie file
		File cookie = new File(tc.getDataDirectory(), "control_auth_cookie");
		cookie.delete();
		PasswordDigest magic = PasswordDigest.generateDigest();
		try {
			FileOutputStream fos = new FileOutputStream(cookie);
			fos.write(magic.getHashedPassword().substring(3).getBytes());
			fos.flush();
			fos.close();
		} catch (Throwable t) {
			return false;
		}
		return true;
	}
}
