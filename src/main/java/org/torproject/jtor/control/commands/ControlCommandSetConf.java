package org.torproject.jtor.control.commands;

import java.util.HashMap;
import java.util.Iterator;

import org.torproject.jtor.TorConfig;
import org.torproject.jtor.config.impl.TorConfigParser;
import org.torproject.jtor.control.ControlConnectionHandler;
import org.torproject.jtor.control.KeyNotFoundException;

public class ControlCommandSetConf {

	public static boolean handleSetConf(ControlConnectionHandler cch, String in) {
		String[] confs = in.split(" ");
		HashMap<String, String> oldvals = new HashMap<String, String>();
		for (int i = 0; i < confs.length; i++) {
			String key, value = "";
			if (confs[i].indexOf("=") < 0) { // only a key
				key = confs[i];
			} else {
				key = confs[i].substring(0, confs[i].indexOf("="));
				value = confs[i].substring(confs[i].indexOf("=")+1);
			}
			
			boolean success;

			try {
				oldvals.put(key, ControlCommandGetConf.handleGetConf(cch, key));

				// TODO reset options that can be specified multiple times

				if (value == null || value.equals("")) {
					// set the default value
					setDefaultConf(cch, key);
					success = true;
				} else {
					//replace with new value
					success = TorConfigParser.setConf(cch.getControlServer().getTorConfig(), key, value);
				}


			} catch (KeyNotFoundException e) {
				cch.getControlServer().getLogger().warning("Control command setconf key not found: " + key);
				
				success = false;
			}
			if (!success) {
				//restore all settings done by this command because one has failed
				Iterator<String> it = oldvals.keySet().iterator();
				while (it.hasNext()) {
					String oldkey = (String)it.next();
					String oldval = (String)oldvals.get(oldkey);
					TorConfigParser.setConf(cch.getControlServer().getTorConfig(), oldkey, oldval);
				}
				cch.write("552 Unrecognized option");
				return false;
			}
		}
		cch.write("250 configuration values set");
		return true;
	}

	public static void setDefaultConf(ControlConnectionHandler cch, String key) throws KeyNotFoundException {
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
		
		else if (key.equals("tunneldirconns")) {
			tc.setDefaultTunnelDirConns();
		}
		
		else if (key.equals("prefertunneleddirconns")) {
			tc.setDefaultPreferTunneledDirConns();
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
		
		else if (key.equals("reachableaddresses")) {
			tc.setDefaultReachableAddresses();
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

		else if (key.equals("sockslistenaddress")) {
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

		else if (key.equals("__alldiroptionsprivate")) {
			tc.setDefault__AllDirOptionsPrivate();
		}

		else if (key.equals("__disablepredictedcircuits")) {
			tc.setDefault__DisablePredictedCircuits();
		}

		else if (key.equals("__leavestreamsunattached")) {
			tc.setDefault__LeaveStreamsUnattached();
		}

		else if (key.equals("__hashedcontrolsessionpassword")) {
			tc.setDefault__HashedControlSessionPassword();
		}

		else if (key.equals("__reloadtorrconsighup")) {
			tc.setDefault__ReloadTorrcOnSIGHUP();
		}

		else {
			throw new KeyNotFoundException();
		}

	}
}
