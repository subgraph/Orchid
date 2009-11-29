package org.torproject.jtor;

import java.io.File;
import java.net.InetAddress;

public interface TorConfig {

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getDataDirectory()
	 */
	public String getDataDirectory();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#loadConf()
	 */
	public void loadConf();

	/** Loads and overwrites all the settings with defaults, if the config file does not
	 * contain all configuration options, it is advised to call this method first to
	 * ensure proper initialization. */
	public void loadDefaults();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#saveConf()
	 */
	public void saveConf();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getConfigFile()
	 */
	public String getConfigFile();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setConfigFile(java.lang.String)
	 */
	public void setConfigFile(String configFile);

	public void setDefaultConfigFile();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getBandwidthRate()
	 */
	public long getBandwidthRate();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setBandwidthRate(long)
	 */
	public void setBandwidthRate(long bandwidthRate);

	public void setDefaultBandwidthRate();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getBandwidthBurst()
	 */
	public long getBandwidthBurst();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setBandwidthBurst(long)
	 */
	public void setBandwidthBurst(long bandwidthBurst);

	public void setDefaultBandwidthBurst();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getMaxAdvertisedBandwidth()
	 */
	public long getMaxAdvertisedBandwidth();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setMaxAdvertisedBandwidth(long)
	 */
	public void setMaxAdvertisedBandwidth(long maxAdvertisedBandwidth);

	public void setDefaultMaxAdvertisedBandwidth();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getControlPort()
	 */
	public short getControlPort();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setControlPort(short)
	 */
	public void setControlPort(short controlPort);

	public void setDefaultControlPort();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getHashedControlPassword()
	 */
	public String getHashedControlPassword();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setHashedControlPassword(java.lang.String)
	 */
	public void setHashedControlPassword(String hashedControlPassword);

	public void setDefaultHashedControlPassword();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#isCookieAuthentication()
	 */
	public boolean isCookieAuthentication();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setCookieAuthentication(boolean)
	 */
	public void setCookieAuthentication(boolean cookieAuthentication);

	public void setDefaultCookieAuthentication();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getDirFetchPeriod()
	 */
	public long getDirFetchPeriod();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDirFetchPeriod(long)
	 */
	public void setDirFetchPeriod(long dirFetchPeriod);

	public void setDefaultDirFetchPeriod();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getDirServer()
	 */
	public String[] getDirServer();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDirServer(java.lang.String[])
	 */
	public void setDirServer(String[] dirServer);

	public void setDefaultDirServer();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#isDisableAllSwap()
	 */
	public boolean isDisableAllSwap();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDisableAllSwap(boolean)
	 */
	public void setDisableAllSwap(boolean disableAllSwap);

	public void setDefaultDisableAllSwap();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getGroup()
	 */
	public String getGroup();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setGroup(java.lang.String)
	 */
	public void setGroup(String group);

	public void setDefaultGroup();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getHttpProxy()
	 */
	public String getHttpProxy();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setHttpProxy(java.lang.String)
	 */
	public void setHttpProxy(String httpProxy);

	public void setDefaultHttpProxy();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getHttpProxyAuthenticator()
	 */
	public String getHttpProxyAuthenticator();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setHttpProxyAuthenticator(java.lang.String)
	 */
	public void setHttpProxyAuthenticator(String httpProxyAuthenticator);

	public void setDefaultHttpProxyAuthenticator();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getHttpsProxy()
	 */
	public String getHttpsProxy();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setHttpsProxy(java.lang.String)
	 */
	public void setHttpsProxy(String httpsProxy);

	public void setDefaultHttpsProxy();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getHttpsProxyAuthenticator()
	 */
	public String getHttpsProxyAuthenticator();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setHttpsProxyAuthenticator(java.lang.String)
	 */
	public void setHttpsProxyAuthenticator(String httpsProxyAuthenticator);

	public void setDefaultHttpsProxyAuthenticator();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getKeepalivePeriod()
	 */
	public int getKeepalivePeriod();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setKeepalivePeriod(int)
	 */
	public void setKeepalivePeriod(int keepalivePeriod);

	public void setDefaultKeepalivePeriod();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getLog()
	 */
	public String[] getLog();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setLog(java.lang.String[])
	 */
	public void setLog(String[] log);

	public void setDefaultLog();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getMaxConn()
	 */
	public int getMaxConn();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setMaxConn(int)
	 */
	public void setMaxConn(int maxConn);

	public void setDefaultMaxConn();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getOutboundBindAddress()
	 */
	public InetAddress getOutboundBindAddress();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setOutboundBindAddress(java.net.InetAddress)
	 */
	public void setOutboundBindAddress(InetAddress outboundBindAddress);

	public void setDefaultOutboundBindAddress();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getPidFile()
	 */
	public String getPidFile();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setPidFile(java.lang.String)
	 */
	public void setPidFile(String pidFile);

	public void setDefaultPidFile();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#isRunAsDaemon()
	 */
	public boolean isRunAsDaemon();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setRunAsDaemon(boolean)
	 */
	public void setRunAsDaemon(boolean runAsDaemon);

	public void setDefaultRunAsDaemon();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#isSafeLogging()
	 */
	public boolean isSafeLogging();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setSafeLogging(boolean)
	 */
	public void setSafeLogging(boolean safeLogging);

	public void setDefaultSafeLogging();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getStatusFetchPeriod()
	 */
	public long getStatusFetchPeriod();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setStatusFetchPeriod(long)
	 */
	public void setStatusFetchPeriod(long statusFetchPeriod);

	public void setDefaultStatusFetchPeriod();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getUser()
	 */
	public String getUser();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setUser(java.lang.String)
	 */
	public void setUser(String user);

	public void setDefaultUser();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#isHardwareAccel()
	 */
	public boolean isHardwareAccel();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setHardwareAccel(boolean)
	 */
	public void setHardwareAccel(boolean hardwareAccel);

	public void setDefaultHardwareAccel();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getAllowUnverifiedNodes()
	 */
	public String getAllowUnverifiedNodes();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setAllowUnverifiedNodes(java.lang.String)
	 */
	public void setAllowUnverifiedNodes(String allowUnverifiedNodes);

	public void setDefaultAllowUnverifiedNodes();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#isClientOnly()
	 */
	public boolean isClientOnly();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setClientOnly(boolean)
	 */
	public void setClientOnly(boolean clientOnly);

	public void setDefaultClientOnly();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getEntryNodes()
	 */
	public String[] getEntryNodes();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setEntryNodes(java.lang.String[])
	 */
	public void setEntryNodes(String[] entryNodes);

	public void setDefaultEntryNodes();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getExitNodes()
	 */
	public String[] getExitNodes();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setExitNodes(java.lang.String[])
	 */
	public void setExitNodes(String[] exitNodes);

	public void setDefaultExitNodes();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getExcludeNodes()
	 */
	public String[] getExcludeNodes();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setExcludeNodes(java.lang.String[])
	 */
	public void setExcludeNodes(String[] excludeNodes);

	public void setDefaultExcludeNodes();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#isStrictExitNodes()
	 */
	public boolean isStrictExitNodes();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setStrictExitNodes(boolean)
	 */
	public void setStrictExitNodes(boolean strictExitNodes);

	public void setDefaultStrictExitNodes();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#isStrictEntryNodes()
	 */
	public boolean isStrictEntryNodes();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setStrictEntryNodes(boolean)
	 */
	public void setStrictEntryNodes(boolean strictEntryNodes);

	public void setDefaultStrictEntryNodes();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#isFascistFirewall()
	 */
	public boolean isFascistFirewall();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setFascistFirewall(boolean)
	 */
	public void setFascistFirewall(boolean fascistFirewall);

	public void setDefaultFascistFirewall();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getFirewallPorts()
	 */
	public short[] getFirewallPorts();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setFirewallPorts(short[])
	 */
	public void setFirewallPorts(short[] firewallPorts);

	public void setDefaultFirewallPorts();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getFirewallIPs()
	 */
	public String[] getFirewallIPs();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setFirewallIPs(java.lang.String[])
	 */
	public void setFirewallIPs(String[] firewallIPs);

	public void setDefaultFirewallIPs();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getLongLivedPorts()
	 */
	public short[] getLongLivedPorts();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setLongLivedPorts(short[])
	 */
	public void setLongLivedPorts(short[] longLivedPorts);

	public void setDefaultLongLivedPorts();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getMapAddress()
	 */
	public String[] getMapAddress();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setMapAddress(java.lang.String[])
	 */
	public void setMapAddress(String[] mapAddress);

	public void setDefaultMapAddress();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getNewCircuitPeriod()
	 */
	public long getNewCircuitPeriod();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setNewCircuitPeriod(long)
	 */
	public void setNewCircuitPeriod(long newCircuitPeriod);

	public void setDefaultNewCircuitPeriod();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getMaxCircuitDirtiness()
	 */
	public long getMaxCircuitDirtiness();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setMaxCircuitDirtiness(long)
	 */
	public void setMaxCircuitDirtiness(long maxCircuitDirtiness);

	public void setDefaultMaxCircuitDirtiness();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getNodeFamily()
	 */
	public String[] getNodeFamily();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setNodeFamily(java.lang.String[])
	 */
	public void setNodeFamily(String[] nodeFamily);

	public void setDefaultNodeFamily();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getRendNodes()
	 */
	public String[] getRendNodes();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setRendNodes(java.lang.String[])
	 */
	public void setRendNodes(String[] rendNodes);

	public void setDefaultRendNodes();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getRendExcludeNodes()
	 */
	public String[] getRendExcludeNodes();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setRendExcludeNodes(java.lang.String[])
	 */
	public void setRendExcludeNodes(String[] rendExcludeNodes);

	public void setDefaultRendExcludeNodes();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getSocksPort()
	 */
	public short getSocksPort();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setSocksPort(short)
	 */
	public void setSocksPort(short socksPort);

	public void setDefaultSocksPort();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getSocksBindAddress()
	 */
	public String getSocksBindAddress();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setSocksBindAddress(java.lang.String)
	 */
	public void setSocksBindAddress(String socksBindAddress);

	public void setDefaultSocksBindAddress();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getSocksPolicy()
	 */
	public String getSocksPolicy();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setSocksPolicy(java.lang.String)
	 */
	public void setSocksPolicy(String socksPolicy);

	public void setDefaultSocksPolicy();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getTrackHostExits()
	 */
	public String[] getTrackHostExits();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setTrackHostExits(java.lang.String[])
	 */
	public void setTrackHostExits(String[] trackHostExits);

	public void setDefaultTrackHostExits();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getTrackHostExitsExpire()
	 */
	public long getTrackHostExitsExpire();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setTrackHostExitsExpire(long)
	 */
	public void setTrackHostExitsExpire(long trackHostExitsExpire);

	public void setDefaultTrackHostExitsExpire();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#isUseHelperNodes()
	 */
	public boolean isUseHelperNodes();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setUseHelperNodes(boolean)
	 */
	public void setUseHelperNodes(boolean useHelperNodes);

	public void setDefaultUseHelperNodes();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getNumHelperNodes()
	 */
	public int getNumHelperNodes();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setNumHelperNodes(int)
	 */
	public void setNumHelperNodes(int numHelperNodes);

	public void setDefaultNumHelperNodes();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getHiddenServiceDir()
	 */
	public String[] getHiddenServiceDir();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setHiddenServiceDir(java.lang.String[])
	 */
	public void setHiddenServiceDir(String[] hiddenServiceDir);

	public void setDefaultHiddenServiceDir();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getHiddenServicePort()
	 */
	public String[] getHiddenServicePort();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setHiddenServicePort(java.lang.String[])
	 */
	public void setHiddenServicePort(String[] hiddenServicePort);

	public void setDefaultHiddenServicePort();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getHiddenServiceNodes()
	 */
	public String[] getHiddenServiceNodes();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setHiddenServiceNodes(java.lang.String[])
	 */
	public void setHiddenServiceNodes(String[] hiddenServiceNodes);

	public void setDefaultHiddenServiceNodes();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getHiddenServiceExcludeNodes()
	 */
	public String[] getHiddenServiceExcludeNodes();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setHiddenServiceExcludeNodes(java.lang.String[])
	 */
	public void setHiddenServiceExcludeNodes(String[] hiddenServiceExcludeNodes);

	public void setDefaultHiddenServiceExcludeNodes();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getHiddenServiceVersion()
	 */
	public String getHiddenServiceVersion();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setHiddenServiceVersion(java.lang.String)
	 */
	public void setHiddenServiceVersion(String hiddenServiceVersion);

	public void setDefaultHiddenServiceVersion();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getRendPostPeriod()
	 */
	public long getRendPostPeriod();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setRendPostPeriod(long)
	 */
	public void setRendPostPeriod(long rendPostPeriod);

	public void setDefaultRendPostPeriod();

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDataDirectory(java.io.File)
	 */
	public void setDataDirectory(File dataDirectory);

	public void setDefaultDataDirectory();

}