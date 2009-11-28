package org.torproject.jtor.config.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;

import org.torproject.jtor.TorConfig;

/**
 *
 * @author Merlijn Hofstra
 */
public class TorConfigParserImpl {

	private TorConfigParserImpl() {}

	public static boolean parseFile(TorConfig tc, File in) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(in));
			String line = null;
			while ((line=reader.readLine()) != null) {
				if (line.startsWith("#") || line.matches("^\\s*$")) { // skip comments and empty lines
					continue;
				}

				// strip comments after a setting
				int index = line.indexOf("#");
				if (index != -1 && !line.substring(index-1, index).equals("\\")) {
					line = line.substring(0, index-1);
				}

				//strip any trailing whitespace
				line = line.replaceAll("\\s*$", "");

				int separator = line.indexOf(" ");
				String key = line.substring(0, separator);
				String value = line.substring(separator+1);

				if (!setConf(tc, key, value)) {
					// syntax error in file
					return false;
				}

			}

		} catch (FileNotFoundException ex) {
			//no config file available
			return true;
		} catch (IOException e) {
			return false;
		}

		return true;
	}

	public static boolean setConf(TorConfig tc, String key, String value) {
		try {
			key = key.toLowerCase();

			if (key.equals("configfile")) {
				tc.setConfigFile(value);
			}

			else if (key.equals("datadirectory")) {
				tc.setDataDirectory(new File(value));
			}

			else if (key.equals("bandwidthrate")) {
				tc.setBandwidthRate(Long.parseLong(value));
			}
			
			else if (key.equals("bandwidthburst")) {
				tc.setBandwidthBurst(Long.parseLong(value));
			}
			
			else if (key.equals("maxadvertisedbandwidth")) {
				tc.setMaxAdvertisedBandwidth(Long.parseLong(value));
			}
			
			else if (key.equals("controlport")) {
				tc.setControlPort(Short.parseShort(value));
			}
			
			else if (key.equals("hashedcontrolpassword")) {
				tc.setHashedControlPassword(value);
			}
			
			else if (key.equals("cookieauthentication")) {
				tc.setCookieAuthentication(isTrue(value));
			}
			
			else if (key.equals("dirfetchperiod")) {
				tc.setDirFetchPeriod(Long.parseLong(value));
			}
			
			else if (key.equals("dirserver")) {
				String[] newar = addToStringArray(tc.getDirServer(), value);
				tc.setDirServer(newar);
			}
			
			else if (key.equals("disableallswap")) {
				tc.setDisableAllSwap(isTrue(value));
			}
			
			else if (key.equals("group")) {
				tc.setGroup(value);
			}
			
			else if (key.equals("httpproxy")) {
				tc.setHttpProxy(value);
			}
			
			else if (key.equals("httpproxyauthenticator")) {
				tc.setHttpProxyAuthenticator(value);
			}
			
			else if (key.equals("httpsproxy")) {
				tc.setHttpsProxy(value);
			}
			
			else if (key.equals("httpsproxyauthenticator")) {
				tc.setHttpsProxyAuthenticator(value);
			}
			
			else if (key.equals("keepaliveperiod")) {
				tc.setKeepalivePeriod(Integer.parseInt(value));
			}
			
			else if (key.equals("log")) {
				String[] newar = addToStringArray(tc.getLog(), value);
				tc.setLog(newar);
			}
			
			else if (key.equals("maxconn")) {
				tc.setMaxConn(Integer.parseInt(value));
			}
			
			else if (key.equals("outboundbindaddress")) {
				tc.setOutboundBindAddress(InetAddress.getByName(value));
			}
			
			else if (key.equals("pidfile")) {
				tc.setPidFile(value);
			}
			
			else if (key.equals("runasdaemon")) {
				tc.setRunAsDaemon(isTrue(value));
			}
			
			else if (key.equals("safelogging")) {
				tc.setSafeLogging(isTrue(value));
			}
			
			else if (key.equals("statusfetchperiod")) {
				tc.setStatusFetchPeriod(Long.parseLong(value));
			}

			else { // key was not found
				return false;
			}
			
		} catch(Throwable t) { // if any errors occur, the operation failed.
			return false;
		}

		return true;
	}

	public static String[] addToStringArray(String[] ar, String val) {
		if (ar == null) {
			String[] ret = new String[1];
			ret[0] = val;
			return ret;
		}

		String[] ret = new String[ar.length+1];
		System.arraycopy(ar, 0, ret, 0, ar.length);
		ret[ar.length] = val;
		return ret;
	}

	public static short[] addToShortArray(short[] ar, short val) {
		if (ar == null) {
			short[] ret = new short[1];
			ret[0] = val;
			return ret;
		}

		short[] ret = new short[ar.length+1];
		System.arraycopy(ar, 0, ret, 0, ar.length);
		ret[ar.length] = val;
		return ret;
	}
	
	public static boolean isTrue(String in) {
		return !(in.equals("0") || in.equals("false"));
	}

}
