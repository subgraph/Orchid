package org.torproject.jtor.config.impl;

import java.io.File;
import java.net.InetAddress;

import org.torproject.jtor.Logger;
import org.torproject.jtor.TorConfig;

public class TorConfigImpl implements TorConfig {

	private String configFile;
	private File dataDirectory;

	private long bandwidthRate;
	private long bandwidthBurst;
	private long maxAdvertisedBandwidth;

	private short controlPort;
	private String hashedControlPassword;
	private boolean cookieAuthentication;

	private long dirFetchPeriod;
	private String[] dirServer;

	private boolean disableAllSwap;
	private String group;

	private String httpProxy;
	private String httpProxyAuthenticator;
	private String httpsProxy;
	private String httpsProxyAuthenticator;

	private int keepalivePeriod;

	private String[] log;

	private int maxConn;
	private InetAddress outboundBindAddress;
	private String pidFile;
	private boolean runAsDaemon;

	private boolean safeLogging;

	private long statusFetchPeriod;

	private String user;

	private boolean hardwareAccel;

	private String allowUnverifiedNodes;
	private boolean clientOnly;

	private String[] entryNodes;
	private String[] exitNodes;
	private String[] excludeNodes;
	private boolean strictExitNodes;
	private boolean strictEntryNodes;

	private boolean fascistFirewall;
	private short[] firewallPorts;
	private String[] firewallIPs;

	private short[] longLivedPorts;

	private String[] mapAddress;

	private long newCircuitPeriod;
	private long maxCircuitDirtiness;

	private String[] nodeFamily;
	private String[] rendNodes;
	private String[] rendExcludeNodes;

	private short socksPort;
	private String socksBindAddress;
	private String socksPolicy;

	private String[] trackHostExits;
	private long trackHostExitsExpire;

	private boolean useHelperNodes;

	private int numHelperNodes;

	private String[] hiddenServiceDir;
	private String[] hiddenServicePort;
	private String[] hiddenServiceNodes;
	private String[] hiddenServiceExcludeNodes;
	private String hiddenServiceVersion;
	private long rendPostPeriod;
	
	// hidden (not saved) options
	private boolean __AllDirOptionsPrivate;
	private boolean __DisablePredictedCircuits;
	private boolean __LeaveStreamsUnattached;
	private String __HashedControlSessionPassword;
	private boolean __ReloadTorrcOnSIGHUP;

	protected Logger logger;

	public TorConfigImpl(Logger logger) {
		this.logger = logger;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getDataDirectory()
	 */
	 public String getDataDirectory() {
		return dataDirectory.getAbsolutePath();
	}

	private File createDataDirectory() {
		if(!dataDirectory.exists())
			dataDirectory.mkdirs();
		return dataDirectory;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#loadConf()
	 */
	public void loadConf() {
		boolean success = TorConfigParserImpl.parseFile(this, logger, new File(dataDirectory, configFile));
		if (!success) {
			System.err.println("Unable to parse config file - Quitting");
			System.exit(1);
		}
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#loadDefaults()
	 */
	public void loadDefaults() {
		TorConfigDefaults tcd = new TorConfigDefaults();
		allowUnverifiedNodes = tcd.getAllowUnverifiedNodes();
		bandwidthBurst = tcd.getBandwidthBurst();
		bandwidthRate = tcd.getBandwidthRate();
		clientOnly = tcd.isClientOnly();
		configFile = tcd.getConfigFile();
		controlPort = tcd.getControlPort();
		cookieAuthentication = tcd.isCookieAuthentication();
		dataDirectory = tcd.getDataDirectory();
		dirFetchPeriod = tcd.getDirFetchPeriod();
		dirServer = tcd.getDirServer();
		disableAllSwap = tcd.isDisableAllSwap();
		entryNodes = tcd.getEntryNodes();
		excludeNodes = tcd.getExcludeNodes();
		exitNodes = tcd.getExitNodes();
		fascistFirewall = tcd.isFascistFirewall();
		firewallIPs = tcd.getFirewallIPs();
		firewallPorts = tcd.getFirewallPorts();
		group = tcd.getGroup();
		hardwareAccel = tcd.isHardwareAccel();
		hashedControlPassword = tcd.getHashedControlPassword();
		hiddenServiceDir = tcd.getHiddenServiceDir();
		hiddenServiceExcludeNodes = tcd.getHiddenServiceExcludeNodes();
		hiddenServiceNodes = tcd.getHiddenServiceNodes();
		hiddenServicePort = tcd.getHiddenServicePort();
		hiddenServiceVersion = tcd.getHiddenServiceVersion();
		httpProxy = tcd.getHttpProxy();
		httpProxyAuthenticator = tcd.getHttpProxyAuthenticator();
		httpsProxy = tcd.getHttpsProxy();
		httpsProxyAuthenticator = tcd.getHttpsProxyAuthenticator();
		keepalivePeriod = tcd.getKeepalivePeriod();
		log = tcd.getLog();
		longLivedPorts = tcd.getLongLivedPorts();
		mapAddress = tcd.getMapAddress();
		maxAdvertisedBandwidth = tcd.getMaxAdvertisedBandwidth();
		maxCircuitDirtiness = tcd.getMaxCircuitDirtiness();
		maxConn = tcd.getMaxConn();
		newCircuitPeriod = tcd.getNewCircuitPeriod();
		nodeFamily = tcd.getNodeFamily();
		numHelperNodes = tcd.getNumHelperNodes();
		outboundBindAddress = tcd.getOutboundBindAddress();
		pidFile = tcd.getPidFile();
		rendExcludeNodes = tcd.getRendExcludeNodes();
		rendNodes = tcd.getRendNodes();
		rendPostPeriod = tcd.getRendPostPeriod();
		runAsDaemon = tcd.isRunAsDaemon();
		safeLogging = tcd.isSafeLogging();
		socksBindAddress = tcd.getSocksBindAddress();
		socksPolicy = tcd.getSocksPolicy();
		socksPort = tcd.getSocksPort();
		statusFetchPeriod = tcd.getStatusFetchPeriod();
		strictEntryNodes = tcd.isStrictEntryNodes();
		strictExitNodes = tcd.isStrictExitNodes();
		trackHostExits = tcd.getTrackHostExits();
		trackHostExitsExpire = tcd.getTrackHostExitsExpire();
		useHelperNodes = tcd.isUseHelperNodes();
		user = tcd.getUser();
		
		__AllDirOptionsPrivate = tcd.is__AllDirOptionsPrivate();
		__DisablePredictedCircuits = tcd.is__DisablePredictedCircuits();
		__HashedControlSessionPassword = tcd.get__HashedControlSessionPassword();
		__LeaveStreamsUnattached = tcd.is__LeaveStreamsUnattached();
		__ReloadTorrcOnSIGHUP = tcd.is__ReloadTorrcOnSIGHUP();
	}


	public boolean saveConf() {
		createDataDirectory();
		return TorConfigSaver.save(new File(dataDirectory, configFile), this);
	}

	public void resetConf() {
		loadDefaults();
		loadConf();
	}
	
	public boolean is__AllDirOptionsPrivate() {
		return __AllDirOptionsPrivate;
	}

	public void set__AllDirOptionsPrivate(boolean allDirOptionsPrivate) {
		__AllDirOptionsPrivate = allDirOptionsPrivate;
	}

	public void setDefault__AllDirOptionsPrivate() {
		__AllDirOptionsPrivate = new TorConfigDefaults().is__AllDirOptionsPrivate();
	}

	public boolean is__DisablePredictedCircuits() {
		return __DisablePredictedCircuits;
	}

	public void set__DisablePredictedCircuits(boolean disablePredictedCircuits) {
		__DisablePredictedCircuits = disablePredictedCircuits;
	}

	public void setDefault__DisablePredictedCircuits() {
		__DisablePredictedCircuits = new TorConfigDefaults().is__DisablePredictedCircuits();
	}

	public boolean is__LeaveStreamsUnattached() {
		return __LeaveStreamsUnattached;
	}

	public void set__LeaveStreamsUnattached(boolean leaveStreamsUnattached) {
		__LeaveStreamsUnattached = leaveStreamsUnattached;
	}

	public void setDefault__LeaveStreamsUnattached() {
		__LeaveStreamsUnattached = new TorConfigDefaults().is__LeaveStreamsUnattached();
	}

	public String get__HashedControlSessionPassword() {
		return __HashedControlSessionPassword;
	}

	public void set__HashedControlSessionPassword(
			String hashedControlSessionPassword) {
		__HashedControlSessionPassword = hashedControlSessionPassword;
	}

	public void setDefault__HashedControlSessionPassword() {
		__HashedControlSessionPassword = new TorConfigDefaults().get__HashedControlSessionPassword();
	}

	public boolean is__ReloadTorrcOnSIGHUP() {
		return __ReloadTorrcOnSIGHUP;
	}

	public void set__ReloadTorrcOnSIGHUP(boolean reloadTorrcOnSIGHUP) {
		__ReloadTorrcOnSIGHUP = reloadTorrcOnSIGHUP;
	}

	public void setDefault__ReloadTorrcOnSIGHUP() {
		__ReloadTorrcOnSIGHUP = new TorConfigDefaults().is__ReloadTorrcOnSIGHUP();
	}

	public String getConfigFile() {
		return configFile;
	}

	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

	public void setDefaultConfigFile() {
		this.configFile = new TorConfigDefaults().getConfigFile();
	}

	public long getBandwidthRate() {
		return bandwidthRate;
	}

	public void setBandwidthRate(long bandwidthRate) {
		this.bandwidthRate = bandwidthRate;
	}

	public void setDefaultBandwidthRate() {
		this.bandwidthRate = new TorConfigDefaults().getBandwidthRate();
	}

	public long getBandwidthBurst() {
		return bandwidthBurst;
	}

	public void setBandwidthBurst(long bandwidthBurst) {
		this.bandwidthBurst = bandwidthBurst;
	}

	public void setDefaultBandwidthBurst() {
		this.bandwidthBurst = new TorConfigDefaults().getBandwidthBurst();
	}

	public long getMaxAdvertisedBandwidth() {
		return maxAdvertisedBandwidth;
	}

	public void setMaxAdvertisedBandwidth(long maxAdvertisedBandwidth) {
		this.maxAdvertisedBandwidth = maxAdvertisedBandwidth;
	}

	public void setDefaultMaxAdvertisedBandwidth() {
		this.maxAdvertisedBandwidth = new TorConfigDefaults().getMaxAdvertisedBandwidth();
	}

	public short getControlPort() {
		return controlPort;
	}

	public void setControlPort(short controlPort) {
		this.controlPort = controlPort;
	}

	public void setDefaultControlPort() {
		this.controlPort = new TorConfigDefaults().getControlPort();
	}

	public String getHashedControlPassword() {
		return hashedControlPassword;
	}

	public void setHashedControlPassword(String hashedControlPassword) {
		this.hashedControlPassword = hashedControlPassword;
	}

	public void setDefaultHashedControlPassword() {
		this.hashedControlPassword = new TorConfigDefaults().getHashedControlPassword();
	}

	public boolean isCookieAuthentication() {
		return cookieAuthentication;
	}

	public void setCookieAuthentication(boolean cookieAuthentication) {
		this.cookieAuthentication = cookieAuthentication;
	}

	public void setDefaultCookieAuthentication() {
		this.cookieAuthentication = new TorConfigDefaults().isCookieAuthentication();
	}

	public long getDirFetchPeriod() {
		return dirFetchPeriod;
	}

	public void setDirFetchPeriod(long dirFetchPeriod) {
		this.dirFetchPeriod = dirFetchPeriod;
	}

	public void setDefaultDirFetchPeriod() {
		this.dirFetchPeriod = new TorConfigDefaults().getDirFetchPeriod();
	}

	public String[] getDirServer() {
		return dirServer;
	}

	public void setDirServer(String[] dirServer) {
		this.dirServer = dirServer;
	}

	public void setDefaultDirServer() {
		this.dirServer = new TorConfigDefaults().getDirServer();
	}

	public boolean isDisableAllSwap() {
		return disableAllSwap;
	}

	public void setDisableAllSwap(boolean disableAllSwap) {
		this.disableAllSwap = disableAllSwap;
	}

	public void setDefaultDisableAllSwap() {
		this.disableAllSwap = new TorConfigDefaults().isDisableAllSwap();
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public void setDefaultGroup() {
		this.group = new TorConfigDefaults().getGroup();
	}

	public String getHttpProxy() {
		return httpProxy;
	}

	public void setHttpProxy(String httpProxy) {
		this.httpProxy = httpProxy;
	}

	public void setDefaultHttpProxy() {
		this.httpProxy = new TorConfigDefaults().getHttpProxy();
	}

	public String getHttpProxyAuthenticator() {
		return httpProxyAuthenticator;
	}

	public void setHttpProxyAuthenticator(String httpProxyAuthenticator) {
		this.httpProxyAuthenticator = httpProxyAuthenticator;
	}

	public void setDefaultHttpProxyAuthenticator() {
		this.httpProxyAuthenticator = new TorConfigDefaults().getHttpProxyAuthenticator();
	}

	public String getHttpsProxy() {
		return httpsProxy;
	}

	public void setHttpsProxy(String httpsProxy) {
		this.httpsProxy = httpsProxy;
	}

	public void setDefaultHttpsProxy() {
		this.httpsProxy = new TorConfigDefaults().getHttpsProxy();
	}

	public String getHttpsProxyAuthenticator() {
		return httpsProxyAuthenticator;
	}

	public void setHttpsProxyAuthenticator(String httpsProxyAuthenticator) {
		this.httpsProxyAuthenticator = httpsProxyAuthenticator;
	}

	public void setDefaultHttpsProxyAuthenticator() {
		this.httpsProxyAuthenticator = new TorConfigDefaults().getHttpsProxyAuthenticator();
	}

	public int getKeepalivePeriod() {
		return keepalivePeriod;
	}

	public void setKeepalivePeriod(int keepalivePeriod) {
		this.keepalivePeriod = keepalivePeriod;
	}

	public void setDefaultKeepalivePeriod() {
		this.keepalivePeriod = new TorConfigDefaults().getKeepalivePeriod();
	}

	public String[] getLog() {
		return log;
	}

	public void setLog(String[] log) {
		this.log = log;
	}

	public void setDefaultLog() {
		this.log = new TorConfigDefaults().getLog();
	}

	public int getMaxConn() {
		return maxConn;
	}

	public void setMaxConn(int maxConn) {
		this.maxConn = maxConn;
	}

	public void setDefaultMaxConn() {
		this.maxConn = new TorConfigDefaults().getMaxConn();
	}

	public InetAddress getOutboundBindAddress() {
		return outboundBindAddress;
	}

	public void setOutboundBindAddress(InetAddress outboundBindAddress) {
		this.outboundBindAddress = outboundBindAddress;
	}

	public void setDefaultOutboundBindAddress() {
		this.outboundBindAddress = new TorConfigDefaults().getOutboundBindAddress();
	}

	public String getPidFile() {
		return pidFile;
	}

	public void setPidFile(String pidFile) {
		this.pidFile = pidFile;
	}

	public void setDefaultPidFile() {
		this.pidFile = new TorConfigDefaults().getPidFile();
	}

	public boolean isRunAsDaemon() {
		return runAsDaemon;
	}

	public void setRunAsDaemon(boolean runAsDaemon) {
		this.runAsDaemon = runAsDaemon;
	}

	public void setDefaultRunAsDaemon() {
		this.runAsDaemon = new TorConfigDefaults().isRunAsDaemon();
	}

	public boolean isSafeLogging() {
		return safeLogging;
	}

	public void setSafeLogging(boolean safeLogging) {
		this.safeLogging = safeLogging;
	}

	public void setDefaultSafeLogging() {
		this.safeLogging = new TorConfigDefaults().isSafeLogging();
	}

	public long getStatusFetchPeriod() {
		return statusFetchPeriod;
	}

	public void setStatusFetchPeriod(long statusFetchPeriod) {
		this.statusFetchPeriod = statusFetchPeriod;
	}

	public void setDefaultStatusFetchPeriod() {
		this.statusFetchPeriod = new TorConfigDefaults().getStatusFetchPeriod();
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public void setDefaultUser() {
		this.user = new TorConfigDefaults().getUser();
	}

	public boolean isHardwareAccel() {
		return hardwareAccel;
	}

	public void setHardwareAccel(boolean hardwareAccel) {
		this.hardwareAccel = hardwareAccel;
	}

	public void setDefaultHardwareAccel() {
		this.hardwareAccel = new TorConfigDefaults().isHardwareAccel();
	}

	public String getAllowUnverifiedNodes() {
		return allowUnverifiedNodes;
	}

	public void setAllowUnverifiedNodes(String allowUnverifiedNodes) {
		this.allowUnverifiedNodes = allowUnverifiedNodes;
	}

	public void setDefaultAllowUnverifiedNodes() {
		this.allowUnverifiedNodes = new TorConfigDefaults().getAllowUnverifiedNodes();
	}

	public boolean isClientOnly() {
		return clientOnly;
	}

	public void setClientOnly(boolean clientOnly) {
		this.clientOnly = clientOnly;
	}

	public void setDefaultClientOnly() {
		this.clientOnly = new TorConfigDefaults().isClientOnly();
	}

	public String[] getEntryNodes() {
		return entryNodes;
	}

	public void setEntryNodes(String[] entryNodes) {
		this.entryNodes = entryNodes;
	}

	public void setDefaultEntryNodes() {
		this.entryNodes = new TorConfigDefaults().getEntryNodes();
	}

	public String[] getExitNodes() {
		return exitNodes;
	}

	public void setExitNodes(String[] exitNodes) {
		this.exitNodes = exitNodes;
	}

	public void setDefaultExitNodes() {
		this.exitNodes = new TorConfigDefaults().getExitNodes();
	}

	public String[] getExcludeNodes() {
		return excludeNodes;
	}

	public void setExcludeNodes(String[] excludeNodes) {
		this.excludeNodes = excludeNodes;
	}

	public void setDefaultExcludeNodes() {
		this.excludeNodes = new TorConfigDefaults().getExcludeNodes();
	}

	public boolean isStrictExitNodes() {
		return strictExitNodes;
	}

	public void setStrictExitNodes(boolean strictExitNodes) {
		this.strictExitNodes = strictExitNodes;
	}

	public void setDefaultStrictExitNodes() {
		this.strictExitNodes = new TorConfigDefaults().isStrictExitNodes();
	}

	public boolean isStrictEntryNodes() {
		return strictEntryNodes;
	}

	public void setStrictEntryNodes(boolean strictEntryNodes) {
		this.strictEntryNodes = strictEntryNodes;
	}

	public void setDefaultStrictEntryNodes() {
		this.strictEntryNodes = new TorConfigDefaults().isStrictEntryNodes();
	}

	public boolean isFascistFirewall() {
		return fascistFirewall;
	}

	public void setFascistFirewall(boolean fascistFirewall) {
		this.fascistFirewall = fascistFirewall;
	}

	public void setDefaultFascistFirewall() {
		this.fascistFirewall = new TorConfigDefaults().isFascistFirewall();
	}

	public short[] getFirewallPorts() {
		return firewallPorts;
	}

	public void setFirewallPorts(short[] firewallPorts) {
		this.firewallPorts = firewallPorts;
	}

	public void setDefaultFirewallPorts() {
		this.firewallPorts = new TorConfigDefaults().getFirewallPorts();
	}

	public String[] getFirewallIPs() {
		return firewallIPs;
	}

	public void setFirewallIPs(String[] firewallIPs) {
		this.firewallIPs = firewallIPs;
	}

	public void setDefaultFirewallIPs() {
		this.firewallIPs = new TorConfigDefaults().getFirewallIPs();
	}

	public short[] getLongLivedPorts() {
		return longLivedPorts;
	}

	public void setLongLivedPorts(short[] longLivedPorts) {
		this.longLivedPorts = longLivedPorts;
	}

	public void setDefaultLongLivedPorts() {
		this.longLivedPorts = new TorConfigDefaults().getLongLivedPorts();
	}

	public String[] getMapAddress() {
		return mapAddress;
	}

	public void setMapAddress(String[] mapAddress) {
		this.mapAddress = mapAddress;
	}

	public void setDefaultMapAddress() {
		this.mapAddress = new TorConfigDefaults().getMapAddress();
	}

	public long getNewCircuitPeriod() {
		return newCircuitPeriod;
	}

	public void setNewCircuitPeriod(long newCircuitPeriod) {
		this.newCircuitPeriod = newCircuitPeriod;
	}

	public void setDefaultNewCircuitPeriod() {
		this.newCircuitPeriod = new TorConfigDefaults().getNewCircuitPeriod();
	}

	public long getMaxCircuitDirtiness() {
		return maxCircuitDirtiness;
	}

	public void setMaxCircuitDirtiness(long maxCircuitDirtiness) {
		this.maxCircuitDirtiness = maxCircuitDirtiness;
	}

	public void setDefaultMaxCircuitDirtiness() {
		this.maxCircuitDirtiness = new TorConfigDefaults().getMaxCircuitDirtiness();
	}

	public String[] getNodeFamily() {
		return nodeFamily;
	}

	public void setNodeFamily(String[] nodeFamily) {
		this.nodeFamily = nodeFamily;
	}

	public void setDefaultNodeFamily() {
		this.nodeFamily = new TorConfigDefaults().getNodeFamily();
	}

	public String[] getRendNodes() {
		return rendNodes;
	}

	public void setRendNodes(String[] rendNodes) {
		this.rendNodes = rendNodes;
	}

	public void setDefaultRendNodes() {
		this.rendNodes = new TorConfigDefaults().getRendNodes();
	}

	public String[] getRendExcludeNodes() {
		return rendExcludeNodes;
	}

	public void setRendExcludeNodes(String[] rendExcludeNodes) {
		this.rendExcludeNodes = rendExcludeNodes;
	}

	public void setDefaultRendExcludeNodes() {
		this.rendExcludeNodes = new TorConfigDefaults().getRendExcludeNodes();
	}

	public short getSocksPort() {
		return socksPort;
	}

	public void setSocksPort(short socksPort) {
		this.socksPort = socksPort;
	}

	public void setDefaultSocksPort() {
		this.socksPort = new TorConfigDefaults().getSocksPort();
	}

	public String getSocksBindAddress() {
		return socksBindAddress;
	}

	public void setSocksBindAddress(String socksBindAddress) {
		this.socksBindAddress = socksBindAddress;
	}

	public void setDefaultSocksBindAddress() {
		this.socksBindAddress = new TorConfigDefaults().getSocksBindAddress();
	}

	public String getSocksPolicy() {
		return socksPolicy;
	}

	public void setSocksPolicy(String socksPolicy) {
		this.socksPolicy = socksPolicy;
	}

	public void setDefaultSocksPolicy() {
		this.socksPolicy = new TorConfigDefaults().getSocksPolicy();
	}

	public String[] getTrackHostExits() {
		return trackHostExits;
	}

	public void setTrackHostExits(String[] trackHostExits) {
		this.trackHostExits = trackHostExits;
	}

	public void setDefaultTrackHostExits() {
		this.trackHostExits = new TorConfigDefaults().getTrackHostExits();
	}

	public long getTrackHostExitsExpire() {
		return trackHostExitsExpire;
	}

	public void setTrackHostExitsExpire(long trackHostExitsExpire) {
		this.trackHostExitsExpire = trackHostExitsExpire;
	}

	public void setDefaultTrackHostExitsExpire() {
		this.trackHostExitsExpire = new TorConfigDefaults().getTrackHostExitsExpire();
	}

	public boolean isUseHelperNodes() {
		return useHelperNodes;
	}

	public void setUseHelperNodes(boolean useHelperNodes) {
		this.useHelperNodes = useHelperNodes;
	}

	public void setDefaultUseHelperNodes() {
		this.useHelperNodes = new TorConfigDefaults().isUseHelperNodes();
	}

	public int getNumHelperNodes() {
		return numHelperNodes;
	}

	public void setNumHelperNodes(int numHelperNodes) {
		this.numHelperNodes = numHelperNodes;
	}

	public void setDefaultNumHelperNodes() {
		this.numHelperNodes = new TorConfigDefaults().getNumHelperNodes();
	}

	public String[] getHiddenServiceDir() {
		return hiddenServiceDir;
	}

	public void setHiddenServiceDir(String[] hiddenServiceDir) {
		this.hiddenServiceDir = hiddenServiceDir;
	}

	public void setDefaultHiddenServiceDir() {
		this.hiddenServiceDir = new TorConfigDefaults().getHiddenServiceDir();
	}

	public String[] getHiddenServicePort() {
		return hiddenServicePort;
	}

	public void setHiddenServicePort(String[] hiddenServicePort) {
		this.hiddenServicePort = hiddenServicePort;
	}

	public void setDefaultHiddenServicePort() {
		this.hiddenServicePort = new TorConfigDefaults().getHiddenServicePort();
	}

	public String[] getHiddenServiceNodes() {
		return hiddenServiceNodes;
	}

	public void setHiddenServiceNodes(String[] hiddenServiceNodes) {
		this.hiddenServiceNodes = hiddenServiceNodes;
	}

	public void setDefaultHiddenServiceNodes() {
		this.hiddenServiceNodes = new TorConfigDefaults().getHiddenServiceNodes();
	}

	public String[] getHiddenServiceExcludeNodes() {
		return hiddenServiceExcludeNodes;
	}

	public void setHiddenServiceExcludeNodes(String[] hiddenServiceExcludeNodes) {
		this.hiddenServiceExcludeNodes = hiddenServiceExcludeNodes;
	}

	public void setDefaultHiddenServiceExcludeNodes() {
		this.hiddenServiceExcludeNodes = new TorConfigDefaults().getHiddenServiceExcludeNodes();
	}

	public String getHiddenServiceVersion() {
		return hiddenServiceVersion;
	}

	public void setHiddenServiceVersion(String hiddenServiceVersion) {
		this.hiddenServiceVersion = hiddenServiceVersion;
	}

	public void setDefaultHiddenServiceVersion() {
		this.hiddenServiceVersion = new TorConfigDefaults().getHiddenServiceVersion();
	}

	public long getRendPostPeriod() {
		return rendPostPeriod;
	}

	public void setRendPostPeriod(long rendPostPeriod) {
		this.rendPostPeriod = rendPostPeriod;
	}

	public void setDefaultRendPostPeriod() {
		this.rendPostPeriod = new TorConfigDefaults().getRendPostPeriod();
	}

	public void setDataDirectory(File dataDirectory) {
		this.dataDirectory = dataDirectory;
	}

	public void setDefaultDataDirectory() {
		this.dataDirectory = new TorConfigDefaults().getDataDirectory();
	}
}
