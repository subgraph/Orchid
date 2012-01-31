package org.torproject.jtor.config.impl;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class TorConfigDefaults {

	private String configFile = "torrc";
    private File dataDirectory = new File(new File(System.getProperty("user.home")), ".jtor");
    
    private long bandwidthRate = 2097152; // 2 MB
    private long bandwidthBurst = 5242880; // 5 MB
    private long maxAdvertisedBandwidth;
    
    private short controlPort;
    private String hashedControlPassword;
    private boolean cookieAuthentication = false;
    
    private long dirFetchPeriod = 3600;
    private String[] dirServer = {
    		"moria1 v1 18.244.0.188:9031 FFCB 46DB 1339 DA84 674C 70D7 CB58 6434 C437 0441",
    		"moria2 v1 18.244.0.114:80 719B E45D E224 B607 C537 07D0 E214 3E2D 423E 74CF",
    		"tor26 v1 86.59.21.38:80 847B 1F85 0344 D787 6491 A548 92F9 0493 4E4E B85D",
    };

	private boolean tunnelDirConns = true;
	private boolean preferTunneledDirConns = true;

	private boolean disableAllSwap = false;
    private String group;
    
    private String httpProxy;
    private String httpProxyAuthenticator;
    private String httpsProxy;
    private String httpsProxyAuthenticator;
    
    private int keepalivePeriod = 300;
    
    private String[] log = new String[0];
    
    private int maxConn = 1024;
    private InetAddress outboundBindAddress;
    private String pidFile;
    private boolean runAsDaemon = false;
    
    private boolean safeLogging = true;
    
    private long statusFetchPeriod = 1800;
    
    private String user;
    
    private boolean hardwareAccel = true;
    
    private String allowUnverifiedNodes = "middle,rendezvous";
    private boolean clientOnly = false;
    
    private String[] entryNodes = new String[0];
    private String[] exitNodes = new String[0];
    private String[] excludeNodes = new String[0];
    private boolean strictExitNodes = false;
    private boolean strictEntryNodes = false;
    
    private boolean fascistFirewall = false;
    private short[] firewallPorts = { 80, 443 };
    private String[] firewallIPs = new String[0];
    private String reachableAddresses = "accept *:*";
    
    private short[] longLivedPorts = { 21, 22, 706, 1863, 5050, 5190, 5222, 5223, 6667, 8300, 8888 };
    
    private String[] mapAddress = new String[0];
    
    private long newCircuitPeriod = 30;
    private long maxCircuitDirtiness = 600;
    
    private String[] nodeFamily = new String[0];
    private String[] rendNodes = new String[0];
    private String[] rendExcludeNodes = new String[0];
    
    private short socksPort = 9050;
    private String socksBindAddress = "127.0.0.1";
    private String socksPolicy;
    
    private String[] trackHostExits = new String[0];
    private long trackHostExitsExpire = 1800;
    
    private boolean useHelperNodes = true;
    private int numHelperNodes = 3;
    
    private String[] hiddenServiceDir = new String[0];
    private String[] hiddenServicePort = new String[0];
    private String[] hiddenServiceNodes = new String[0];
    private String[] hiddenServiceExcludeNodes = new String[0];
    private String hiddenServiceVersion = "0,2";
    private long rendPostPeriod = 1200;
    
    // hidden (not saved) options
	private boolean __AllDirOptionsPrivate = false;
	private boolean __DisablePredictedCircuits = false;
	private boolean __LeaveStreamsUnattached = false;
	private String __HashedControlSessionPassword;
	private boolean __ReloadTorrcOnSIGHUP = true;

	public TorConfigDefaults() {
    	try {
			outboundBindAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {}
    }
    
    public boolean isTunnelDirConns() {
		return tunnelDirConns;
	}

	public boolean isPreferTunneledDirConns() {
		return preferTunneledDirConns;
	}
    
    public boolean is__AllDirOptionsPrivate() {
		return __AllDirOptionsPrivate;
	}

	public boolean is__DisablePredictedCircuits() {
		return __DisablePredictedCircuits;
	}

	public boolean is__LeaveStreamsUnattached() {
		return __LeaveStreamsUnattached;
	}

	public String get__HashedControlSessionPassword() {
		return __HashedControlSessionPassword;
	}

	public boolean is__ReloadTorrcOnSIGHUP() {
		return __ReloadTorrcOnSIGHUP;
	}

	public String getConfigFile() {
		return configFile;
	}

	public File getDataDirectory() {
		return dataDirectory;
	}

	public long getBandwidthRate() {
		return bandwidthRate;
	}

	public long getBandwidthBurst() {
		return bandwidthBurst;
	}

	public long getMaxAdvertisedBandwidth() {
		return maxAdvertisedBandwidth;
	}

	public short getControlPort() {
		return controlPort;
	}

	public String getHashedControlPassword() {
		return hashedControlPassword;
	}

	public boolean isCookieAuthentication() {
		return cookieAuthentication;
	}

	public long getDirFetchPeriod() {
		return dirFetchPeriod;
	}

	public String[] getDirServer() {
		return dirServer;
	}

	public boolean isDisableAllSwap() {
		return disableAllSwap;
	}

	public String getGroup() {
		return group;
	}

	public String getHttpProxy() {
		return httpProxy;
	}

	public String getHttpProxyAuthenticator() {
		return httpProxyAuthenticator;
	}

	public String getHttpsProxy() {
		return httpsProxy;
	}

	public String getHttpsProxyAuthenticator() {
		return httpsProxyAuthenticator;
	}

	public int getKeepalivePeriod() {
		return keepalivePeriod;
	}

	public String[] getLog() {
		return log;
	}

	public int getMaxConn() {
		return maxConn;
	}

	public InetAddress getOutboundBindAddress() {
		return outboundBindAddress;
	}

	public String getPidFile() {
		return pidFile;
	}

	public boolean isRunAsDaemon() {
		return runAsDaemon;
	}

	public boolean isSafeLogging() {
		return safeLogging;
	}

	public long getStatusFetchPeriod() {
		return statusFetchPeriod;
	}

	public String getUser() {
		return user;
	}

	public boolean isHardwareAccel() {
		return hardwareAccel;
	}

	public String getAllowUnverifiedNodes() {
		return allowUnverifiedNodes;
	}

	public boolean isClientOnly() {
		return clientOnly;
	}

	public String[] getEntryNodes() {
		return entryNodes;
	}

	public String[] getExitNodes() {
		return exitNodes;
	}

	public String[] getExcludeNodes() {
		return excludeNodes;
	}

	public boolean isStrictExitNodes() {
		return strictExitNodes;
	}

	public boolean isStrictEntryNodes() {
		return strictEntryNodes;
	}

	public boolean isFascistFirewall() {
		return fascistFirewall;
	}

	public short[] getFirewallPorts() {
		return firewallPorts;
	}

	public String[] getFirewallIPs() {
		return firewallIPs;
	}

	public String getReachableAddresses() {
		return reachableAddresses;
	}

	public short[] getLongLivedPorts() {
		return longLivedPorts;
	}

	public String[] getMapAddress() {
		return mapAddress;
	}

	public long getNewCircuitPeriod() {
		return newCircuitPeriod;
	}

	public long getMaxCircuitDirtiness() {
		return maxCircuitDirtiness;
	}

	public String[] getNodeFamily() {
		return nodeFamily;
	}

	public String[] getRendNodes() {
		return rendNodes;
	}

	public String[] getRendExcludeNodes() {
		return rendExcludeNodes;
	}

	public short getSocksPort() {
		return socksPort;
	}

	public String getSocksBindAddress() {
		return socksBindAddress;
	}

	public String getSocksPolicy() {
		return socksPolicy;
	}

	public String[] getTrackHostExits() {
		return trackHostExits;
	}

	public long getTrackHostExitsExpire() {
		return trackHostExitsExpire;
	}

	public boolean isUseHelperNodes() {
		return useHelperNodes;
	}

	public int getNumHelperNodes() {
		return numHelperNodes;
	}

	public String[] getHiddenServiceDir() {
		return hiddenServiceDir;
	}

	public String[] getHiddenServicePort() {
		return hiddenServicePort;
	}

	public String[] getHiddenServiceNodes() {
		return hiddenServiceNodes;
	}

	public String[] getHiddenServiceExcludeNodes() {
		return hiddenServiceExcludeNodes;
	}

	public String getHiddenServiceVersion() {
		return hiddenServiceVersion;
	}

	public long getRendPostPeriod() {
		return rendPostPeriod;
	}
}
