package org.torproject.jtor.control.commands;

import java.io.File;

import org.torproject.jtor.Tor;
import org.torproject.jtor.TorConfig;
import org.torproject.jtor.control.ControlConnectionHandler;

public class ControlCommandProtocolInfo {

	public static void handleProtocolInfo(ControlConnectionHandler cch) {
		TorConfig tc = cch.getControlServer().getTorConfig();
		cch.write("250-PROTOCOLINFO 1");
		
		String authline = "250-AUTH METHODS=";
		if (tc.getHashedControlPassword() != null) {
			authline += "HASHEDPASSWORD";
		}
		
		else if (tc.isCookieAuthentication()) {
			authline += "COOKIE COOKIEFILE=\"" + tc.getDataDirectory() + File.separator + "control_auth_cookie\"";
		}
		
		else {
			authline += "NULL";
		}
		
		cch.write(authline);
		
		cch.write("250-VERSION Tor=\"" + Tor.getVersion() + "\"");
		
		cch.write("250 OK");
	}
}
