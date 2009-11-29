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

    public boolean verifyCommand(String in) {
        return true;
    }
    
    @SuppressWarnings("unchecked")
	public boolean execute(String in) {
    	if (verifyCommand(in)) {
    		String command = in.substring(0, in.indexOf(" ")).toLowerCase();
    		String args = in.substring(in.indexOf(" ", 0)+1);
    		args = removeQuotes(args);
    		
    		if (command.equals("setconf")) {
    			String[] confs = args.split(" ");
    			HashMap oldvals = new HashMap();
    			for (int i = 0; i < confs.length; i++) {
    				String key = confs[i].substring(0, confs[i].indexOf("="));
    				String value = confs[i].substring(confs[i].indexOf("=")+1);
    				boolean success;
    				
    				try {
						oldvals.put(key, getConf(key));
						success = TorConfigParserImpl.setConf(cch.getControlServer().getTorConfig(), key, value);
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
						return false;
					}
    			}
    			
    			// reply with key=value pairs
    			Iterator it = pairs.keySet().iterator();
    			while (it.hasNext()) {
    				String key = (String)it.next();
    				String[] vals = ((String)pairs.get(key)).split("\n");
    				for (int i = 0; i < vals.length; i++) {
    					if (vals[i] == null || vals[i].equals("")) {
    						// default value
    						cch.write("250 " + key);
    					} else {
    						cch.write("250 " + key + "=" + vals[i]);
    					}
    				}
    			}
    			return true;
    		}
    		
    		else if (command.equals("resetconf")) {
    			String[] confs = args.split(" ");
    			for (int i = 0; i < confs.length; i++) {
    				if (confs[i].indexOf("=") == -1) {
    					//cch.getControlServer().getTorConfig().resetConf(confs[i]);
    				} else {
    					//String key = confs[i].substring(0, confs[i].indexOf("="));
        				//String value = confs[i].substring(confs[i].indexOf("=")+1);
        				//cch.getControlServer().getTorConfig().setConf(key, value);
    				}
    			}
    		}
    		
    		return true;
    	}
    	return false;
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
