package org.torproject.jtor.config.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.torproject.jtor.TorConfig;

public class TorConfigSaver {

	@SuppressWarnings("unchecked")
	public static boolean save(File torrc, TorConfig tc) {
		BufferedReader reader;
		String output = "";
		Map opts = configToHash(tc);
		HashMap written = new HashMap();
		try {
			reader = new BufferedReader(new FileReader(torrc));
			String line = null;
			while ((line=reader.readLine()) != null) {
				if (line.startsWith("#") || line.matches("^\\s*$")) { // copy comments directly
					
					// TODO check comments for values that have changed and inject them here instead of the end of file
					
					output += line + "\n";
					continue;
				}

				String comment = "";
				if (line.indexOf("#") > 0) {
					comment = line.substring(line.indexOf("#"));
					line = line.substring(0, line.indexOf("#"));
				}

				int separator = line.indexOf(" ");
				String key = line.substring(0, separator);

				if (!opts.containsKey(key.toLowerCase())) { // faulty option
					continue;
				}

				if (written.get(key.toLowerCase()) != null) { // already saved
					continue;
				}

				if (((String)opts.get(key.toLowerCase())).indexOf("\n") == -1) {
					output += key + " " + (String)opts.get(key.toLowerCase()) + " " + comment + "\n";
					written.put(key.toLowerCase(), "");
					continue;
				}

				String[] out = ((String)opts.get(key.toLowerCase())).split("\n");
				written.put(key.toLowerCase(), "");
				for (int i = 0; i < out.length; i++) {
					output += key + " " + out[i] + "\n";
				}
			}
		} catch (FileNotFoundException e) {
			torrc.setWritable(true);
		} catch (IOException e) {}
		
		Map defaults = defaultConfigToHash(new TorConfigDefaults());
		
		Iterator it = opts.keySet().iterator();
		while (it.hasNext()) {
			String key = (String)it.next();
			if (key.startsWith("__")) // values with __ should never be stored
				continue;
			
			if (written.get(key) == null && opts.get(key) != null) {
				String val = (String)opts.get(key);
				
				if (((String)defaults.get(key)).equals(val)) { // value hasn't changed
					continue;
				}
				
				if (((String)opts.get(key)).indexOf("\n") == -1) {
					output += key + " " + val + "\n";
				} else {
					String[] out = ((String)opts.get(key)).split("\n");
					for (int i = 0; i < out.length; i++) {
						output += key + " " + out[i] + "\n";
					}
				}
			}
		}
		
		try {
			torrc.delete();
			FileOutputStream fos = new FileOutputStream(torrc);
			fos.write(output.getBytes());
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			return false;
		}
		
		return true;
	}

	@SuppressWarnings("unchecked")
	public static Map configToHash(TorConfig tc) {
		HashMap hm = new HashMap();

		hm.put("configfile", tc.getConfigFile());
		hm.put("datadirectory", tc.getDataDirectory());
		hm.put("bandwidthrate", "" + tc.getBandwidthRate());
		hm.put("bandwidthburst", "" + tc.getBandwidthBurst());
		hm.put("maxadvertisedbandwidth", "" + tc.getMaxAdvertisedBandwidth());
		hm.put("controlport", "" + tc.getControlPort());
		hm.put("hashedcontrolpassword", tc.getHashedControlPassword());
		hm.put("cookieauthentication", "" + (tc.isCookieAuthentication() ? "1" : "0"));
		hm.put("dirfetchperiod", "" + tc.getDirFetchPeriod());

		hm.put("dirserver", arrayToString(tc.getDirServer()));

		hm.put("disableallswap", "" + (tc.isDisableAllSwap() ? "1" : "0"));
		hm.put("group", tc.getGroup());
		hm.put("httpproxy", tc.getHttpProxy());
		hm.put("httpproxyauthenticator", tc.getHttpProxyAuthenticator());
		hm.put("httpsproxy", tc.getHttpsProxy());
		hm.put("httpsproxyauthenticator", tc.getHttpsProxyAuthenticator());
		hm.put("keepaliveperiod", "" + tc.getKeepalivePeriod());
		hm.put("log", arrayToString(tc.getLog()));

		hm.put("maxconn", "" + tc.getMaxConn());
		hm.put("outboundbindaddress", tc.getOutboundBindAddress().getHostAddress());
		hm.put("pidfile", tc.getPidFile());
		hm.put("runasdaemon", "" + (tc.isRunAsDaemon() ? "1" : "0"));
		hm.put("safelogging", "" + (tc.isSafeLogging() ? "1" : "0"));
		hm.put("statusfetchperiod", "" + tc.getStatusFetchPeriod());
		hm.put("user", tc.getUser());
		hm.put("hardwareaccel", "" + (tc.isHardwareAccel() ? "1" : "0"));
		hm.put("allowunverifiednodes", tc.getAllowUnverifiedNodes());
		hm.put("clientonly", "" + (tc.isClientOnly() ? "1" : "0"));
		hm.put("entrynodes", arrayToString(tc.getEntryNodes()));

		hm.put("exitnodes", arrayToString(tc.getExitNodes()));

		hm.put("excludenodes", arrayToString(tc.getExcludeNodes()));

		hm.put("strictexitnodes", "" + (tc.isStrictExitNodes() ? "1" : "0"));
		hm.put("strictentrynodes", "" + (tc.isStrictEntryNodes() ? "1" : "0"));
		hm.put("fascistfirewall", "" + (tc.isFascistFirewall() ? "1" : "0"));
		hm.put("firewallports", arrayToString(tc.getFirewallPorts()));

		hm.put("firewallips", arrayToString(tc.getFirewallIPs()));

		hm.put("longlivedports", arrayToString(tc.getLongLivedPorts()));

		hm.put("mapaddress", arrayToString(tc.getMapAddress()));

		hm.put("newcircuitperiod", "" + tc.getNewCircuitPeriod());
		hm.put("maxcircuitdirtiness", "" + tc.getMaxCircuitDirtiness());
		hm.put("nodefamily", arrayToString(tc.getNodeFamily()));

		hm.put("rendnodes", arrayToString(tc.getRendNodes()));

		hm.put("rendexcludenodes", arrayToString(tc.getRendExcludeNodes()));

		hm.put("socksport", "" + tc.getSocksPort());
		hm.put("socksbindaddress", tc.getSocksBindAddress());
		hm.put("sockspolicy", tc.getSocksPolicy());
		hm.put("trackhostexits", arrayToString(tc.getTrackHostExits()));

		hm.put("trackhostexitsexpire", "" + tc.getTrackHostExitsExpire());
		hm.put("usehelpernodes", "" + (tc.isUseHelperNodes() ? "1" : "0"));
		hm.put("numhelpernodes", "" + tc.getNumHelperNodes());
		hm.put("hiddenservicedir", arrayToString(tc.getHiddenServiceDir()));

		hm.put("hiddenserviceport", arrayToString(tc.getHiddenServicePort()));

		hm.put("hiddenservicenodes", arrayToString(tc.getHiddenServiceNodes()));

		hm.put("hiddenserviceexcludenodes", arrayToString(tc.getHiddenServiceExcludeNodes()));

		hm.put("hiddenserviceversion", tc.getHiddenServiceVersion());
		hm.put("rendpostperiod", "" + tc.getRendPostPeriod());

		return hm;
	}

	@SuppressWarnings("unchecked")
	public static Map defaultConfigToHash(TorConfigDefaults tcd) {
		HashMap hm = new HashMap();

		hm.put("configfile", tcd.getConfigFile());
		hm.put("datadirectory", tcd.getDataDirectory().getAbsolutePath());
		hm.put("bandwidthrate", "" + tcd.getBandwidthRate());
		hm.put("bandwidthburst", "" + tcd.getBandwidthBurst());
		hm.put("maxadvertisedbandwidth", "" + tcd.getMaxAdvertisedBandwidth());
		hm.put("controlport", "" + tcd.getControlPort());
		hm.put("hashedcontrolpassword", tcd.getHashedControlPassword());
		hm.put("cookieauthentication", "" + (tcd.isCookieAuthentication() ? "1" : "0"));
		hm.put("dirfetchperiod", "" + tcd.getDirFetchPeriod());

		hm.put("dirserver", arrayToString(tcd.getDirServer()));

		hm.put("disableallswap", "" + (tcd.isDisableAllSwap() ? "1" : "0"));
		hm.put("group", tcd.getGroup());
		hm.put("httpproxy", tcd.getHttpProxy());
		hm.put("httpproxyauthenticator", tcd.getHttpProxyAuthenticator());
		hm.put("httpsproxy", tcd.getHttpsProxy());
		hm.put("httpsproxyauthenticator", tcd.getHttpsProxyAuthenticator());
		hm.put("keepaliveperiod", "" + tcd.getKeepalivePeriod());
		hm.put("log", arrayToString(tcd.getLog()));

		hm.put("maxconn", "" + tcd.getMaxConn());
		hm.put("outboundbindaddress", tcd.getOutboundBindAddress().getHostAddress());
		hm.put("pidfile", tcd.getPidFile());
		hm.put("runasdaemon", "" + (tcd.isRunAsDaemon() ? "1" : "0"));
		hm.put("safelogging", "" + (tcd.isSafeLogging() ? "1" : "0"));
		hm.put("statusfetchperiod", "" + tcd.getStatusFetchPeriod());
		hm.put("user", tcd.getUser());
		hm.put("hardwareaccel", "" + (tcd.isHardwareAccel() ? "1" : "0"));
		hm.put("allowunverifiednodes", tcd.getAllowUnverifiedNodes());
		hm.put("clientonly", "" + (tcd.isClientOnly() ? "1" : "0"));
		hm.put("entrynodes", arrayToString(tcd.getEntryNodes()));

		hm.put("exitnodes", arrayToString(tcd.getExitNodes()));

		hm.put("excludenodes", arrayToString(tcd.getExcludeNodes()));

		hm.put("strictexitnodes", "" + (tcd.isStrictExitNodes() ? "1" : "0"));
		hm.put("strictentrynodes", "" + (tcd.isStrictEntryNodes() ? "1" : "0"));
		hm.put("fascistfirewall", "" + (tcd.isFascistFirewall() ? "1" : "0"));
		hm.put("firewallports", arrayToString(tcd.getFirewallPorts()));

		hm.put("firewallips", arrayToString(tcd.getFirewallIPs()));

		hm.put("longlivedports", arrayToString(tcd.getLongLivedPorts()));

		hm.put("mapaddress", arrayToString(tcd.getMapAddress()));

		hm.put("newcircuitperiod", "" + tcd.getNewCircuitPeriod());
		hm.put("maxcircuitdirtiness", "" + tcd.getMaxCircuitDirtiness());
		hm.put("nodefamily", arrayToString(tcd.getNodeFamily()));

		hm.put("rendnodes", arrayToString(tcd.getRendNodes()));

		hm.put("rendexcludenodes", arrayToString(tcd.getRendExcludeNodes()));

		hm.put("socksport", "" + tcd.getSocksPort());
		hm.put("socksbindaddress", tcd.getSocksBindAddress());
		hm.put("sockspolicy", tcd.getSocksPolicy());
		hm.put("trackhostexits", arrayToString(tcd.getTrackHostExits()));

		hm.put("trackhostexitsexpire", "" + tcd.getTrackHostExitsExpire());
		hm.put("usehelpernodes", "" + (tcd.isUseHelperNodes() ? "1" : "0"));
		hm.put("numhelpernodes", "" + tcd.getNumHelperNodes());
		hm.put("hiddenservicedir", arrayToString(tcd.getHiddenServiceDir()));

		hm.put("hiddenserviceport", arrayToString(tcd.getHiddenServicePort()));

		hm.put("hiddenservicenodes", arrayToString(tcd.getHiddenServiceNodes()));

		hm.put("hiddenserviceexcludenodes", arrayToString(tcd.getHiddenServiceExcludeNodes()));

		hm.put("hiddenserviceversion", tcd.getHiddenServiceVersion());
		hm.put("rendpostperiod", "" + tcd.getRendPostPeriod());

		return hm;
	}

	public static String arrayToString(String[] in) {
		if (in == null) {
			return "";
		}
		
		String ret = "";
		for (int i = 0; i < in.length; i++) {
			ret += in[i] + "\n";
		}
		ret = ret.replaceAll("\\s*$", "");
		return ret;
	}

	public static String arrayToString(short[] in) {
		if (in == null) {
			return "";
		}
		
		String ret = "";
		for (int i = 0; i < in.length; i++) {
			ret += in[i] + ", ";
		}
		ret = ret.replaceAll("[,\\s]*$", "");
		return ret;
	}
}
