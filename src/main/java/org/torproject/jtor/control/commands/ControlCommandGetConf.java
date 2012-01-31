package org.torproject.jtor.control.commands;

import org.torproject.jtor.TorConfig;
import org.torproject.jtor.control.ControlConnectionHandler;
import org.torproject.jtor.control.KeyNotFoundException;

public class ControlCommandGetConf {

	public static String handleGetConf(ControlConnectionHandler cch, String key) throws KeyNotFoundException {
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
		
		else if (key.equals("tunneldirconns")) {
			return "" + (tc.isTunnelDirConns() ? "1" : "0");
		}
		
		else if (key.equals("prefertunneleddirconns")) {
			return "" + (tc.isPreferTunneledDirConns() ? "1" : "0");
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
		
		else if (key.equals("reachableaddresses")) {
			return tc.getReachableAddresses();
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

		else if (key.equals("sockslistenaddress")) {
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
		
		else if (key.equals("__alldiroptionsprivate")) {
			return "" + (tc.is__AllDirOptionsPrivate() ? "1" : "0");
		}
		
		else if (key.equals("__disablepredictedcircuits")) {
			return "" + (tc.is__DisablePredictedCircuits() ? "1" : "0");
		}
		
		else if (key.equals("__leavestreamsunattached")) {
			return "" + (tc.is__LeaveStreamsUnattached() ? "1" : "0");
		}
		
		else if (key.equals("__hashedcontrolsessionpassword")) {
			return tc.get__HashedControlSessionPassword();
		}
		
		else if (key.equals("__reloadtorrconsighup")) {
			return "" + (tc.is__ReloadTorrcOnSIGHUP() ? "1" : "0");
		}

		throw new KeyNotFoundException();
	}
}
