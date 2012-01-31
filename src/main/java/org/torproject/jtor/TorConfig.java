package org.torproject.jtor;

import java.io.File;
import java.net.InetAddress;

import org.torproject.jtor.events.EventHandler;

public interface TorConfig {

	public String getDataDirectory();

	public void loadConf();

	/** Loads and overwrites all the settings with defaults, if the config file does not
	 * contain all configuration options, it is advised to call this method first to
	 * ensure proper initialization.	public void loadDefaults();
	 */
	public void loadDefaults();

	public boolean saveConf();
	
	public void registerConfigChangedHandler(EventHandler eh);
	
	public void unregisterConfigChangedHandler(EventHandler eh);
		
	public boolean is__AllDirOptionsPrivate();

	public void set__AllDirOptionsPrivate(boolean allDirOptionsPrivate);

	public void setDefault__AllDirOptionsPrivate();

	public boolean is__DisablePredictedCircuits();

	public void set__DisablePredictedCircuits(boolean disablePredictedCircuits);

	public void setDefault__DisablePredictedCircuits();

	public boolean is__LeaveStreamsUnattached();

	public void set__LeaveStreamsUnattached(boolean leaveStreamsUnattached);

	public void setDefault__LeaveStreamsUnattached();

	public String get__HashedControlSessionPassword();

	public void set__HashedControlSessionPassword(String hashedControlSessionPassword);

	public void setDefault__HashedControlSessionPassword();

	public boolean is__ReloadTorrcOnSIGHUP();

	public void set__ReloadTorrcOnSIGHUP(boolean reloadTorrcOnSIGHUP);

	public void setDefault__ReloadTorrcOnSIGHUP();

	public String getConfigFile();

	public void setConfigFile(String configFile);

	public void setDefaultConfigFile();
	
	public String getReachableAddresses();
	
	public void setReachableAddresses(String ReachableAddresses);
	
	public void setDefaultReachableAddresses();
	
	public void setDefaultPreferTunneledDirConns();
	
	public void setPreferTunneledDirConns(boolean preferTunneledDirConns);
	
	public boolean isPreferTunneledDirConns();
	
	public void setTunnelDirConns(boolean tunnelDirConns);
	
	public void setDefaultTunnelDirConns();
	
	public boolean isTunnelDirConns();

	public long getBandwidthRate();

	public void setBandwidthRate(long bandwidthRate);

	public void setDefaultBandwidthRate();

	public long getBandwidthBurst();

	public void setBandwidthBurst(long bandwidthBurst);

	public void setDefaultBandwidthBurst();

	public long getMaxAdvertisedBandwidth();

	public void setMaxAdvertisedBandwidth(long maxAdvertisedBandwidth);

	public void setDefaultMaxAdvertisedBandwidth();

	public short getControlPort();

	public void setControlPort(short controlPort);

	public void setDefaultControlPort();

	public String getHashedControlPassword();

	public void setHashedControlPassword(String hashedControlPassword);

	public void setDefaultHashedControlPassword();

	public boolean isCookieAuthentication();

	public void setCookieAuthentication(boolean cookieAuthentication);

	public void setDefaultCookieAuthentication();

	public long getDirFetchPeriod();

	public void setDirFetchPeriod(long dirFetchPeriod);

	public void setDefaultDirFetchPeriod();

	public String[] getDirServer();

	public void setDirServer(String[] dirServer);

	public void setDefaultDirServer();

	public boolean isDisableAllSwap();

	public void setDisableAllSwap(boolean disableAllSwap);

	public void setDefaultDisableAllSwap();

	public String getGroup();

	public void setGroup(String group);

	public void setDefaultGroup();

	public String getHttpProxy();

	public void setHttpProxy(String httpProxy);

	public void setDefaultHttpProxy();

	public String getHttpProxyAuthenticator();

	public void setHttpProxyAuthenticator(String httpProxyAuthenticator);

	public void setDefaultHttpProxyAuthenticator();

	public String getHttpsProxy();

	public void setHttpsProxy(String httpsProxy);

	public void setDefaultHttpsProxy();

	public String getHttpsProxyAuthenticator();

	public void setHttpsProxyAuthenticator(String httpsProxyAuthenticator);

	public void setDefaultHttpsProxyAuthenticator();

	public int getKeepalivePeriod();

	public void setKeepalivePeriod(int keepalivePeriod);

	public void setDefaultKeepalivePeriod();

	public String[] getLog();

	public void setLog(String[] log);

	public void setDefaultLog();

	public int getMaxConn();

	public void setMaxConn(int maxConn);

	public void setDefaultMaxConn();

	public InetAddress getOutboundBindAddress();

	public void setOutboundBindAddress(InetAddress outboundBindAddress);

	public void setDefaultOutboundBindAddress();

	public String getPidFile();

	public void setPidFile(String pidFile);

	public void setDefaultPidFile();

	public boolean isRunAsDaemon();

	public void setRunAsDaemon(boolean runAsDaemon);

	public void setDefaultRunAsDaemon();

	public boolean isSafeLogging();

	public void setSafeLogging(boolean safeLogging);

	public void setDefaultSafeLogging();

	public long getStatusFetchPeriod();

	public void setStatusFetchPeriod(long statusFetchPeriod);

	public void setDefaultStatusFetchPeriod();

	public String getUser();

	public void setUser(String user);

	public void setDefaultUser();

	public boolean isHardwareAccel();

	public void setHardwareAccel(boolean hardwareAccel);

	public void setDefaultHardwareAccel();

	public String getAllowUnverifiedNodes();

	public void setAllowUnverifiedNodes(String allowUnverifiedNodes);

	public void setDefaultAllowUnverifiedNodes();

	public boolean isClientOnly();

	public void setClientOnly(boolean clientOnly);

	public void setDefaultClientOnly();

	public String[] getEntryNodes();

	public void setEntryNodes(String[] entryNodes);

	public void setDefaultEntryNodes();

	public String[] getExitNodes();

	public void setExitNodes(String[] exitNodes);

	public void setDefaultExitNodes();

	public String[] getExcludeNodes();

	public void setExcludeNodes(String[] excludeNodes);

	public void setDefaultExcludeNodes();

	public boolean isStrictExitNodes();

	public void setStrictExitNodes(boolean strictExitNodes);

	public void setDefaultStrictExitNodes();

	public boolean isStrictEntryNodes();

	public void setStrictEntryNodes(boolean strictEntryNodes);

	public void setDefaultStrictEntryNodes();

	public boolean isFascistFirewall();

	public void setFascistFirewall(boolean fascistFirewall);

	public void setDefaultFascistFirewall();

	public short[] getFirewallPorts();

	public void setFirewallPorts(short[] firewallPorts);

	public void setDefaultFirewallPorts();

	public String[] getFirewallIPs();

	public void setFirewallIPs(String[] firewallIPs);

	public void setDefaultFirewallIPs();

	public short[] getLongLivedPorts();

	public void setLongLivedPorts(short[] longLivedPorts);

	public void setDefaultLongLivedPorts();

	public String[] getMapAddress();

	public void setMapAddress(String[] mapAddress);

	public void setDefaultMapAddress();

	public long getNewCircuitPeriod();

	public void setNewCircuitPeriod(long newCircuitPeriod);

	public void setDefaultNewCircuitPeriod();

	public long getMaxCircuitDirtiness();

	public void setMaxCircuitDirtiness(long maxCircuitDirtiness);

	public void setDefaultMaxCircuitDirtiness();

	public String[] getNodeFamily();

	public void setNodeFamily(String[] nodeFamily);

	public void setDefaultNodeFamily();

	public String[] getRendNodes();

	public void setRendNodes(String[] rendNodes);

	public void setDefaultRendNodes();

	public String[] getRendExcludeNodes();

	public void setRendExcludeNodes(String[] rendExcludeNodes);

	public void setDefaultRendExcludeNodes();

	public short getSocksPort();

	public void setSocksPort(short socksPort);

	public void setDefaultSocksPort();

	public String getSocksBindAddress();

	public void setSocksBindAddress(String socksBindAddress);

	public void setDefaultSocksBindAddress();

	public String getSocksPolicy();

	public void setSocksPolicy(String socksPolicy);

	public void setDefaultSocksPolicy();

	public String[] getTrackHostExits();

	public void setTrackHostExits(String[] trackHostExits);

	public void setDefaultTrackHostExits();

	public long getTrackHostExitsExpire();

	public void setTrackHostExitsExpire(long trackHostExitsExpire);

	public void setDefaultTrackHostExitsExpire();

	public boolean isUseHelperNodes();

	public void setUseHelperNodes(boolean useHelperNodes);

	public void setDefaultUseHelperNodes();

	public int getNumHelperNodes();

	public void setNumHelperNodes(int numHelperNodes);

	public void setDefaultNumHelperNodes();

	public String[] getHiddenServiceDir();

	public void setHiddenServiceDir(String[] hiddenServiceDir);

	public void setDefaultHiddenServiceDir();

	public String[] getHiddenServicePort();

	public void setHiddenServicePort(String[] hiddenServicePort);

	public void setDefaultHiddenServicePort();

	public String[] getHiddenServiceNodes();

	public void setHiddenServiceNodes(String[] hiddenServiceNodes);

	public void setDefaultHiddenServiceNodes();

	public String[] getHiddenServiceExcludeNodes();

	public void setHiddenServiceExcludeNodes(String[] hiddenServiceExcludeNodes);

	public void setDefaultHiddenServiceExcludeNodes();

	public String getHiddenServiceVersion();

	public void setHiddenServiceVersion(String hiddenServiceVersion);

	public void setDefaultHiddenServiceVersion();

	public long getRendPostPeriod();

	public void setRendPostPeriod(long rendPostPeriod);

	public void setDefaultRendPostPeriod();

	public void setDataDirectory(File dataDirectory);

	public void setDefaultDataDirectory();

}