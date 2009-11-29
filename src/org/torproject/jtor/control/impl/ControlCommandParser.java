package org.torproject.jtor.control.impl;

import java.util.HashMap;
import java.util.Iterator;

import org.torproject.jtor.TorConfig;
import org.torproject.jtor.config.impl.TorConfigParserImpl;
import org.torproject.jtor.control.ControlConnectionHandler;

/**
 *
 * @author Merlijn Hofstra
 */
public class ControlCommandParser {

	private ControlConnectionHandler cch;

	public ControlCommandParser(ControlConnectionHandler cch) {
		this.cch = cch;
	}

	@SuppressWarnings("unchecked")
	public void execute(String in) {
		String command = in.substring(0, in.indexOf(" ")).toLowerCase();
		String args = in.substring(in.indexOf(" ", 0)+1);
		args = removeQuotes(args);

		if (command.equals("setconf")) {
			handleSetConf(args);
		}

		else if (command.equals("resetconf")) {
			handleSetConf(args);
		}

		else if (command.equals("getconf")) {
			String[] confs = args.split(" ");
			HashMap pairs = new HashMap(); 
			for (int i = 0; i < confs.length; i++) {
				try {
					if (confs[i].toLowerCase().equals("hiddenserviceoptions")) {
						pairs.put("HiddenServiceDir", getConf("HiddenServiceDir"));
						pairs.put("HiddenServicePort", getConf("HiddenServicePort"));
						pairs.put("HiddenServiceNodes", getConf("HiddenServiceNodes"));
						pairs.put("HiddenServiceExcludeNodes", getConf("HiddenServiceExcludeNodes"));
					} else {
						String value = getConf(confs[i]);
						pairs.put(confs[i], value);
					}
				} catch (KeyNotFoundException e) {
					cch.write("552 unknown configuration keyword");
				}
			}

			// reply with key=value pairs
			Iterator it = pairs.keySet().iterator();
			while (it.hasNext()) {
				String key = (String)it.next();
				String val = ((String)pairs.get(key));

				if (val == null) {
					cch.write("250 " + key);
				}
				String[] vals = val.split("\n");
				for (int i = 0; i < vals.length; i++) {
					if (vals[i] == null || vals[i].equals("")) {
						// default value
						cch.write("250 " + key);
					} else {
						cch.write("250 " + key + "=" + vals[i]);
					}
				}
			}
		}

		else if (command.equals("signal")) {
			if (handleSignal(args)) {
				cch.write("250 OK");
			} else {
				cch.write("552 Unrecognized signal");
			}
		}
	}

	/**
	 * Handles a signal from an incoming control connection
	 * @param in - the signal to be executed
	 * @return true if successful, false if signal not found/supported
	 */
	public boolean handleSignal(String in) {
		in = in.toLowerCase();
		TorConfig tc = cch.getControlServer().getTorConfig();

		if (in.equals("reload")) {
			tc.loadDefaults();
			tc.loadConf();
		}

		else if (in.equals("shutdown")) {
			System.exit(0);
		}

		else if (in.equals("dump")) {
			// TODO what needs to be dumped here?
		}

		else if (in.equals("debug")) {
			// TODO we currently have just the debug level
		}

		else if (in.equals("halt")) {
			System.exit(0);
		}

		else if (in.equals("cleardnscache")) {
			java.security.Security.setProperty("networkaddress.cache.ttl" , "0");
			Thread.yield();
			System.gc();
			java.security.Security.setProperty("networkaddress.cache.ttl" , "-1");
		}

		else if (in.equals("newnym")) {
			java.security.Security.setProperty("networkaddress.cache.ttl" , "0");
			Thread.yield();
			System.gc();
			java.security.Security.setProperty("networkaddress.cache.ttl" , "-1");

			// TODO make new circuits and new connections must use these circuits
		}

		else {
			return false;
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	public boolean handleSetConf(String in) {
		String[] confs = in.split(" ");
		HashMap oldvals = new HashMap();
		for (int i = 0; i < confs.length; i++) {
			String key = confs[i].substring(0, confs[i].indexOf("="));
			String value = confs[i].substring(confs[i].indexOf("=")+1);
			boolean success;

			try {
				oldvals.put(key, getConf(key));

				// TODO reset options that can be specified multiple times

				if (value == null || value.equals("")) {
					// set the default value
					setDefaultConf(key);
					success = true;
				} else {
					//replace with new value
					success = TorConfigParserImpl.setConf(cch.getControlServer().getTorConfig(), key, value);
				}


			} catch (KeyNotFoundException e) {
				success = false;
			}
			if (!success) {
				//restore all settings done by this command because one has failed
				Iterator it = oldvals.keySet().iterator();
				while (it.hasNext()) {
					String oldkey = (String)it.next();
					String oldval = (String)oldvals.get(oldkey);
					TorConfigParserImpl.setConf(cch.getControlServer().getTorConfig(), oldkey, oldval);
				}
				cch.write("552 Unrecognized option");
				return false;
			}
		}
		cch.write("250 configuration values set");
		return true;
	}

	public String getConf(String key) throws KeyNotFoundException {
		TorConfig tc = cch.getControlServer().getTorConfig();
		key = key.toLowerCase();

		if (key.equals("configfile")) {
			return tc.getConfigFile();
		}

		else if (key.equals("datadirectory")) {
			return tc.getDataDirectory();
		}

		else if (key.equals("bandwidthrate")) {
			return "" + tc.getBandwidthRate();
		}

		else if (key.equals("bandwidthburst")) {
			return "" + tc.getBandwidthBurst();
		}

		else if (key.equals("maxadvertisedbandwidth")) {
			return "" + tc.getMaxAdvertisedBandwidth();
		}

		else if (key.equals("controlport")) {
			return "" + tc.getControlPort();
		}

		else if (key.equals("hashedcontrolpassword")) {
			return tc.getHashedControlPassword();
		}

		else if (key.equals("cookieauthentication")) {
			return "" + (tc.isCookieAuthentication() ? "1" : "0");
		}

		else if (key.equals("dirfetchperiod")) {
			return "" + tc.getDirFetchPeriod();
		}

		else if (key.equals("dirserver")) {
			String ret = "";
			String[] val = tc.getDirServer();
			for (int i = 0; i < val.length; i++) {
				ret += val[i] + "\n";
			}
			ret = ret.replaceAll("\\s*$", "");
			return ret;
		}

		else if (key.equals("disableallswap")) {
			return "" + (tc.isDisableAllSwap() ? "1" : "0");
		}

		else if (key.equals("group")) {
			return tc.getGroup();
		}

		else if (key.equals("httpproxy")) {
			return tc.getHttpProxy();
		}

		else if (key.equals("httpproxyauthenticator")) {
			return tc.getHttpProxyAuthenticator();
		}

		else if (key.equals("httpsproxy")) {
			return tc.getHttpsProxy();
		}

		else if (key.equals("httpsproxyauthenticator")) {
			return tc.getHttpsProxyAuthenticator();
		}

		else if (key.equals("keepaliveperiod")) {
			return "" + tc.getKeepalivePeriod();
		}

		else if (key.equals("log")) {
			String ret = "";
			String[] val = tc.getLog();
			for (int i = 0; i < val.length; i++) {
				ret += val[i] + "\n";
			}
			ret = ret.replaceAll("\\s*$", "");
			return ret;
		}

		else if (key.equals("maxconn")) {
			return "" + tc.getMaxConn();
		}

		else if (key.equals("outboundbindaddress")) {
			return tc.getOutboundBindAddress().getHostAddress();
		}

		else if (key.equals("pidfile")) {
			return tc.getPidFile();
		}

		else if (key.equals("runasdaemon")) {
			return "" + (tc.isRunAsDaemon() ? "1" : "0");
		}

		else if (key.equals("safelogging")) {
			return "" + (tc.isSafeLogging() ? "1" : "0");
		}

		else if (key.equals("statusfetchperiod")) {
			return "" + tc.getStatusFetchPeriod();
		}

		else if (key.equals("user")) {
			return tc.getUser();
		}

		else if (key.equals("hardwareaccel")) {
			return "" + (tc.isHardwareAccel() ? "1" : "0");
		}

		else if (key.equals("allowunverifiednodes")) {
			return tc.getAllowUnverifiedNodes();
		}

		else if (key.equals("clientonly")) {
			return "" + (tc.isClientOnly() ? "1" : "0");
		}

		else if (key.equals("entrynodes")) {
			String ret = "";
			String[] val = tc.getEntryNodes();
			for (int i = 0; i < val.length; i++) {
				ret += val[i] + "\n";
			}
			ret = ret.replaceAll("\\s*$", "");
			return ret;
		}

		else if (key.equals("exitnodes")) {
			String ret = "";
			String[] val = tc.getExitNodes();
			for (int i = 0; i < val.length; i++) {
				ret += val[i] + "\n";
			}
			ret = ret.replaceAll("\\s*$", "");
			return ret;
		}

		else if (key.equals("excludenodes")) {
			String ret = "";
			String[] val = tc.getExcludeNodes();
			for (int i = 0; i < val.length; i++) {
				ret += val[i] + "\n";
			}
			ret = ret.replaceAll("\\s*$", "");
			return ret;
		}

		else if (key.equals("strictexitnodes")) {
			return "" + (tc.isStrictExitNodes() ? "1" : "0");
		}

		else if (key.equals("strictentrynodes")) {
			return "" + (tc.isStrictEntryNodes() ? "1" : "0");
		}

		else if (key.equals("fascistfirewall")) {
			return "" + (tc.isFascistFirewall() ? "1" : "0");
		}

		else if (key.equals("firewallports")) {
			String ret = "";
			short[] val = tc.getFirewallPorts();
			for (int i = 0; i < val.length; i++) {
				ret += val[i] + "\n";
			}
			ret = ret.replaceAll("\\s*$", "");
			return ret;
		}

		else if (key.equals("firewallips")) {
			String ret = "";
			String[] val = tc.getFirewallIPs();
			for (int i = 0; i < val.length; i++) {
				ret += val[i] + "\n";
			}
			ret = ret.replaceAll("\\s*$", "");
			return ret;
		}

		else if (key.equals("longlivedports")) {
			String ret = "";
			short[] val = tc.getLongLivedPorts();
			for (int i = 0; i < val.length; i++) {
				ret += val[i] + "\n";
			}
			ret = ret.replaceAll("\\s*$", "");
			return ret;
		}

		else if (key.equals("mapaddress")) {
			String ret = "";
			String[] val = tc.getMapAddress();
			for (int i = 0; i < val.length; i++) {
				ret += val[i] + "\n";
			}
			ret = ret.replaceAll("\\s*$", "");
			return ret;
		}

		else if (key.equals("newcircuitperiod")) {
			return "" + tc.getNewCircuitPeriod();
		}

		else if (key.equals("maxcircuitdirtiness")) {
			return "" + tc.getMaxCircuitDirtiness();
		}

		else if (key.equals("nodefamily")) {
			String ret = "";
			String[] val = tc.getNodeFamily();
			for (int i = 0; i < val.length; i++) {
				ret += val[i] + "\n";
			}
			ret = ret.replaceAll("\\s*$", "");
			return ret;
		}

		else if (key.equals("rendnodes")) {
			String ret = "";
			String[] val = tc.getRendNodes();
			for (int i = 0; i < val.length; i++) {
				ret += val[i] + "\n";
			}
			ret = ret.replaceAll("\\s*$", "");
			return ret;
		}

		else if (key.equals("rendexcludenodes")) {
			String ret = "";
			String[] val = tc.getRendExcludeNodes();
			for (int i = 0; i < val.length; i++) {
				ret += val[i] + "\n";
			}
			ret = ret.replaceAll("\\s*$", "");
			return ret;
		}

		else if (key.equals("socksport")) {
			return "" + tc.getSocksPort();
		}

		else if (key.equals("socksbindaddress")) {
			return tc.getSocksBindAddress();
		}

		else if (key.equals("sockspolicy")) {
			return tc.getSocksPolicy();
		}

		else if (key.equals("trackhostexits")) {
			String ret = "";
			String[] val = tc.getTrackHostExits();
			for (int i = 0; i < val.length; i++) {
				ret += val[i] + "\n";
			}
			ret = ret.replaceAll("\\s*$", "");
			return ret;
		}

		else if (key.equals("trackhostexitsexpire")) {
			return "" + tc.getTrackHostExitsExpire();
		}

		else if (key.equals("usehelpernodes")) {
			return "" + (tc.isUseHelperNodes() ? "1" : "0");
		}

		else if (key.equals("numhelpernodes")) {
			return "" + tc.getNumHelperNodes();
		}

		else if (key.equals("hiddenservicedir")) {
			String ret = "";
			String[] val = tc.getHiddenServiceDir();
			for (int i = 0; i < val.length; i++) {
				ret += val[i] + "\n";
			}
			ret = ret.replaceAll("\\s*$", "");
			return ret;
		}

		else if (key.equals("hiddenserviceport")) {
			String ret = "";
			String[] val = tc.getHiddenServicePort();
			for (int i = 0; i < val.length; i++) {
				ret += val[i] + "\n";
			}
			ret = ret.replaceAll("\\s*$", "");
			return ret;
		}

		else if (key.equals("hiddenservicenodes")) {
			String ret = "";
			String[] val = tc.getHiddenServiceNodes();
			for (int i = 0; i < val.length; i++) {
				ret += val[i] + "\n";
			}
			ret = ret.replaceAll("\\s*$", "");
			return ret;
		}

		else if (key.equals("hiddenserviceexcludenodes")) {
			String ret = "";
			String[] val = tc.getHiddenServiceExcludeNodes();
			for (int i = 0; i < val.length; i++) {
				ret += val[i] + "\n";
			}
			ret = ret.replaceAll("\\s*$", "");
			return ret;
		}

		else if (key.equals("hiddenserviceversion")) {
			return tc.getHiddenServiceVersion();
		}

		else if (key.equals("rendpostperiod")) {
			return "" + tc.getRendPostPeriod();
		}

		throw new KeyNotFoundException();
	}

	public void setDefaultConf(String key) throws KeyNotFoundException {
		TorConfig tc = cch.getControlServer().getTorConfig();
		key = key.toLowerCase();

		if (key.equals("configfile")) {
			tc.setDefaultConfigFile();
		}

		else if (key.equals("datadirectory")) {
			tc.setDefaultDataDirectory();
		}

		else if (key.equals("bandwidthrate")) {
			tc.setDefaultBandwidthRate();
		}

		else if (key.equals("bandwidthburst")) {
			tc.setDefaultBandwidthBurst();
		}

		else if (key.equals("maxadvertisedbandwidth")) {
			tc.setDefaultMaxAdvertisedBandwidth();
		}

		else if (key.equals("controlport")) {
			tc.setDefaultControlPort();
		}

		else if (key.equals("hashedcontrolpassword")) {
			tc.setDefaultHashedControlPassword();
		}

		else if (key.equals("cookieauthentication")) {
			tc.isCookieAuthentication();
		}

		else if (key.equals("dirfetchperiod")) {
			tc.setDefaultDirFetchPeriod();
		}

		else if (key.equals("dirserver")) {
			tc.setDefaultDirServer();
		}

		else if (key.equals("disableallswap")) {
			tc.setDefaultDisableAllSwap();
		}

		else if (key.equals("group")) {
			tc.setDefaultGroup();
		}

		else if (key.equals("httpproxy")) {
			tc.setDefaultHttpProxy();
		}

		else if (key.equals("httpproxyauthenticator")) {
			tc.setDefaultHttpProxyAuthenticator();
		}

		else if (key.equals("httpsproxy")) {
			tc.setDefaultHttpsProxy();
		}

		else if (key.equals("httpsproxyauthenticator")) {
			tc.setDefaultHttpsProxyAuthenticator();
		}

		else if (key.equals("keepaliveperiod")) {
			tc.setDefaultKeepalivePeriod();
		}

		else if (key.equals("log")) {
			tc.setDefaultLog();
		}

		else if (key.equals("maxconn")) {
			tc.setDefaultMaxConn();
		}

		else if (key.equals("outboundbindaddress")) {
			tc.setDefaultOutboundBindAddress();
		}

		else if (key.equals("pidfile")) {
			tc.setDefaultPidFile();
		}

		else if (key.equals("runasdaemon")) {
			tc.setDefaultRunAsDaemon();
		}

		else if (key.equals("safelogging")) {
			tc.setDefaultSafeLogging();
		}

		else if (key.equals("statusfetchperiod")) {
			tc.setDefaultStatusFetchPeriod();
		}

		else if (key.equals("user")) {
			tc.setDefaultUser();
		}

		else if (key.equals("hardwareaccel")) {
			tc.setDefaultHardwareAccel();
		}

		else if (key.equals("allowunverifiednodes")) {
			tc.setDefaultAllowUnverifiedNodes();
		}

		else if (key.equals("clientonly")) {
			tc.setDefaultClientOnly();
		}

		else if (key.equals("entrynodes")) {
			tc.setDefaultEntryNodes();
		}

		else if (key.equals("exitnodes")) {
			tc.setDefaultExitNodes();
		}

		else if (key.equals("excludenodes")) {
			tc.setDefaultExcludeNodes();
		}

		else if (key.equals("strictexitnodes")) {
			tc.setDefaultStrictExitNodes();
		}

		else if (key.equals("strictentrynodes")) {
			tc.setDefaultStrictEntryNodes();
		}

		else if (key.equals("fascistfirewall")) {
			tc.setDefaultFascistFirewall();
		}

		else if (key.equals("firewallports")) {
			tc.setDefaultFirewallPorts();
		}

		else if (key.equals("firewallips")) {
			tc.setDefaultFirewallIPs();
		}

		else if (key.equals("longlivedports")) {
			tc.setDefaultLongLivedPorts();
		}

		else if (key.equals("mapaddress")) {
			tc.setDefaultMapAddress();
		}

		else if (key.equals("newcircuitperiod")) {
			tc.setDefaultNewCircuitPeriod();
		}

		else if (key.equals("maxcircuitdirtiness")) {
			tc.setDefaultMaxCircuitDirtiness();
		}

		else if (key.equals("nodefamily")) {
			tc.setDefaultNodeFamily();
		}

		else if (key.equals("rendnodes")) {
			tc.setDefaultRendNodes();
		}

		else if (key.equals("rendexcludenodes")) {
			tc.setDefaultRendExcludeNodes();
		}

		else if (key.equals("socksport")) {
			tc.setDefaultSocksPort();
		}

		else if (key.equals("socksbindaddress")) {
			tc.setDefaultSocksBindAddress();
		}

		else if (key.equals("sockspolicy")) {
			tc.setDefaultSocksPolicy();
		}

		else if (key.equals("trackhostexits")) {
			tc.setDefaultTrackHostExits();
		}

		else if (key.equals("trackhostexitsexpire")) {
			tc.setDefaultTrackHostExitsExpire();
		}

		else if (key.equals("usehelpernodes")) {
			tc.setDefaultUseHelperNodes();
		}

		else if (key.equals("numhelpernodes")) {
			tc.setDefaultNumHelperNodes();
		}

		else if (key.equals("hiddenservicedir")) {
			tc.setDefaultHiddenServiceDir();
		}

		else if (key.equals("hiddenserviceport")) {
			tc.setDefaultHiddenServicePort();
		}

		else if (key.equals("hiddenservicenodes")) {
			tc.setDefaultHiddenServiceNodes();
		}

		else if (key.equals("hiddenserviceexcludenodes")) {
			tc.setDefaultHiddenServiceExcludeNodes();
		}

		else if (key.equals("hiddenserviceversion")) {
			tc.setDefaultHiddenServiceVersion();
		}

		else if (key.equals("rendpostperiod")) {
			tc.setDefaultRendPostPeriod();
		}

		else {
			throw new KeyNotFoundException();
		}

	}

	/** Removes any unescaped quotes from a given string */
	public static String removeQuotes(String in) {
		int index = 0;
		while (index < in.length()) {
			index = in.indexOf("\"", index);
			if (!in.substring(index-1, index).equals("\\")) {
				//remove the quote as it's not escaped
				in = in.substring(0, index) + in.substring(index+1);
			}
		}
		return in;
	}
}
