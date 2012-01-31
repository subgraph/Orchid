package org.torproject.jtor.config.impl;

import java.io.File;
import java.net.InetAddress;

import org.torproject.jtor.TorConfig;
import org.torproject.jtor.events.EventHandler;
import org.torproject.jtor.events.EventManager;
import org.torproject.jtor.logging.LogManager;
import org.torproject.jtor.logging.Logger;

public class TorConfigImpl implements TorConfig {

	private EventManager configChangedManager = new EventManager();
	private boolean configChanged = false;

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
	private boolean tunnelDirConns;
	private boolean preferTunneledDirConns;

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
	private String reachableAddresses;

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

	public TorConfigImpl(LogManager logManager) {
		this.logger = logManager.getLogger("config");
		new TorConfigEventThread(this);
		loadDefaults();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getDataDirectory()
	 */
	public String getDataDirectory() {
		createDataDirectory();
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
		boolean success = TorConfigParser.parseFile(this, logger, new File(dataDirectory, configFile));
		if (!success) {
			System.err.println("Unable to parse config file - Quitting");
			System.exit(1);
		}
		setConfigChanged(true);
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
		preferTunneledDirConns = tcd.isPreferTunneledDirConns();
		reachableAddresses = tcd.getReachableAddresses();
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
		tunnelDirConns = tcd.isTunnelDirConns();
		useHelperNodes = tcd.isUseHelperNodes();
		user = tcd.getUser();

		__AllDirOptionsPrivate = tcd.is__AllDirOptionsPrivate();
		__DisablePredictedCircuits = tcd.is__DisablePredictedCircuits();
		__HashedControlSessionPassword = tcd.get__HashedControlSessionPassword();
		__LeaveStreamsUnattached = tcd.is__LeaveStreamsUnattached();
		__ReloadTorrcOnSIGHUP = tcd.is__ReloadTorrcOnSIGHUP();

		setConfigChanged(true);
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
		setConfigChanged(true);
	}

	public void setDefault__AllDirOptionsPrivate() {
		__AllDirOptionsPrivate = new TorConfigDefaults().is__AllDirOptionsPrivate();
		setConfigChanged(true);
	}

	public boolean is__DisablePredictedCircuits() {
		return __DisablePredictedCircuits;
	}

	public void set__DisablePredictedCircuits(boolean disablePredictedCircuits) {
		__DisablePredictedCircuits = disablePredictedCircuits;
		setConfigChanged(true);
	}

	public void setDefault__DisablePredictedCircuits() {
		__DisablePredictedCircuits = new TorConfigDefaults().is__DisablePredictedCircuits();
		setConfigChanged(true);
	}

	public boolean is__LeaveStreamsUnattached() {
		return __LeaveStreamsUnattached;
	}

	public void set__LeaveStreamsUnattached(boolean leaveStreamsUnattached) {
		__LeaveStreamsUnattached = leaveStreamsUnattached;
		setConfigChanged(true);
	}

	public void setDefault__LeaveStreamsUnattached() {
		__LeaveStreamsUnattached = new TorConfigDefaults().is__LeaveStreamsUnattached();
		setConfigChanged(true);
	}

	public String get__HashedControlSessionPassword() {
		return __HashedControlSessionPassword;
	}

	public void set__HashedControlSessionPassword(
			String hashedControlSessionPassword) {
		setConfigChanged(true);
		__HashedControlSessionPassword = hashedControlSessionPassword;
	}

	public void setDefault__HashedControlSessionPassword() {
		__HashedControlSessionPassword = new TorConfigDefaults().get__HashedControlSessionPassword();
		setConfigChanged(true);
	}

	public boolean is__ReloadTorrcOnSIGHUP() {
		return __ReloadTorrcOnSIGHUP;
	}

	public void set__ReloadTorrcOnSIGHUP(boolean reloadTorrcOnSIGHUP) {
		__ReloadTorrcOnSIGHUP = reloadTorrcOnSIGHUP;
		setConfigChanged(true);
	}

	public void setDefault__ReloadTorrcOnSIGHUP() {
		__ReloadTorrcOnSIGHUP = new TorConfigDefaults().is__ReloadTorrcOnSIGHUP();
		setConfigChanged(true);
	}

	public String getConfigFile() {
		return configFile;
	}

	public void setConfigFile(String configFile) {
		this.configFile = configFile;
		setConfigChanged(true);
	}

	public void setDefaultConfigFile() {
		this.configFile = new TorConfigDefaults().getConfigFile();
		setConfigChanged(true);
	}

	public long getBandwidthRate() {
		return bandwidthRate;
	}

	public void setBandwidthRate(long bandwidthRate) {
		this.bandwidthRate = bandwidthRate;
		setConfigChanged(true);
	}

	public void setDefaultBandwidthRate() {
		this.bandwidthRate = new TorConfigDefaults().getBandwidthRate();
		setConfigChanged(true);
	}

	public long getBandwidthBurst() {
		return bandwidthBurst;
	}

	public void setBandwidthBurst(long bandwidthBurst) {
		this.bandwidthBurst = bandwidthBurst;
		setConfigChanged(true);
	}

	public void setDefaultBandwidthBurst() {
		this.bandwidthBurst = new TorConfigDefaults().getBandwidthBurst();
		setConfigChanged(true);
	}

	public long getMaxAdvertisedBandwidth() {
		return maxAdvertisedBandwidth;
	}

	public void setMaxAdvertisedBandwidth(long maxAdvertisedBandwidth) {
		this.maxAdvertisedBandwidth = maxAdvertisedBandwidth;
		setConfigChanged(true);
	}

	public void setDefaultMaxAdvertisedBandwidth() {
		this.maxAdvertisedBandwidth = new TorConfigDefaults().getMaxAdvertisedBandwidth();
		setConfigChanged(true);
	}

	public short getControlPort() {
		return controlPort;
	}

	public void setControlPort(short controlPort) {
		this.controlPort = controlPort;
		setConfigChanged(true);
	}

	public void setDefaultControlPort() {
		this.controlPort = new TorConfigDefaults().getControlPort();
		setConfigChanged(true);
	}

	public String getHashedControlPassword() {
		return hashedControlPassword;
	}

	public void setHashedControlPassword(String hashedControlPassword) {
		this.hashedControlPassword = hashedControlPassword;
		setConfigChanged(true);
	}

	public void setDefaultHashedControlPassword() {
		this.hashedControlPassword = new TorConfigDefaults().getHashedControlPassword();
		setConfigChanged(true);
	}

	public boolean isCookieAuthentication() {
		return cookieAuthentication;
	}

	public void setCookieAuthentication(boolean cookieAuthentication) {
		this.cookieAuthentication = cookieAuthentication;
		setConfigChanged(true);
	}

	public void setDefaultCookieAuthentication() {
		this.cookieAuthentication = new TorConfigDefaults().isCookieAuthentication();
		setConfigChanged(true);
	}

	public long getDirFetchPeriod() {
		return dirFetchPeriod;
	}

	public void setDirFetchPeriod(long dirFetchPeriod) {
		this.dirFetchPeriod = dirFetchPeriod;
		setConfigChanged(true);
	}

	public void setDefaultDirFetchPeriod() {
		this.dirFetchPeriod = new TorConfigDefaults().getDirFetchPeriod();
		setConfigChanged(true);
	}

	public String[] getDirServer() {
		return dirServer;
	}

	public void setDirServer(String[] dirServer) {
		this.dirServer = dirServer;
		setConfigChanged(true);
	}

	public void setDefaultDirServer() {
		this.dirServer = new TorConfigDefaults().getDirServer();
		setConfigChanged(true);
	}

	public boolean isTunnelDirConns() {
		return tunnelDirConns;
	}

	public void setTunnelDirConns(boolean tunnelDirConns) {
		this.tunnelDirConns = tunnelDirConns;
		setConfigChanged(true);
	}

	public void setDefaultTunnelDirConns() {
		this.tunnelDirConns = new TorConfigDefaults().isTunnelDirConns();
		setConfigChanged(true);
	}

	public boolean isPreferTunneledDirConns() {
		return preferTunneledDirConns;
	}

	public void setPreferTunneledDirConns(boolean preferTunneledDirConns) {
		this.preferTunneledDirConns = preferTunneledDirConns;
		setConfigChanged(true);
	}

	public void setDefaultPreferTunneledDirConns() {
		this.preferTunneledDirConns = new TorConfigDefaults().isPreferTunneledDirConns();
		setConfigChanged(true);
	}

	public boolean isDisableAllSwap() {
		return disableAllSwap;
	}

	public void setDisableAllSwap(boolean disableAllSwap) {
		this.disableAllSwap = disableAllSwap;
		setConfigChanged(true);
	}

	public void setDefaultDisableAllSwap() {
		this.disableAllSwap = new TorConfigDefaults().isDisableAllSwap();
		setConfigChanged(true);
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
		setConfigChanged(true);
	}

	public void setDefaultGroup() {
		this.group = new TorConfigDefaults().getGroup();
		setConfigChanged(true);
	}

	public String getHttpProxy() {
		return httpProxy;
	}

	public void setHttpProxy(String httpProxy) {
		this.httpProxy = httpProxy;
		setConfigChanged(true);
	}

	public void setDefaultHttpProxy() {
		this.httpProxy = new TorConfigDefaults().getHttpProxy();
		setConfigChanged(true);
	}

	public String getHttpProxyAuthenticator() {
		return httpProxyAuthenticator;
	}

	public void setHttpProxyAuthenticator(String httpProxyAuthenticator) {
		this.httpProxyAuthenticator = httpProxyAuthenticator;
		setConfigChanged(true);
	}

	public void setDefaultHttpProxyAuthenticator() {
		this.httpProxyAuthenticator = new TorConfigDefaults().getHttpProxyAuthenticator();
		setConfigChanged(true);
	}

	public String getHttpsProxy() {
		return httpsProxy;
	}

	public void setHttpsProxy(String httpsProxy) {
		this.httpsProxy = httpsProxy;
		setConfigChanged(true);
	}

	public void setDefaultHttpsProxy() {
		this.httpsProxy = new TorConfigDefaults().getHttpsProxy();
		setConfigChanged(true);
	}

	public String getHttpsProxyAuthenticator() {
		return httpsProxyAuthenticator;
	}

	public void setHttpsProxyAuthenticator(String httpsProxyAuthenticator) {
		this.httpsProxyAuthenticator = httpsProxyAuthenticator;
		setConfigChanged(true);
	}

	public void setDefaultHttpsProxyAuthenticator() {
		this.httpsProxyAuthenticator = new TorConfigDefaults().getHttpsProxyAuthenticator();
		setConfigChanged(true);
	}

	public int getKeepalivePeriod() {
		return keepalivePeriod;
	}

	public void setKeepalivePeriod(int keepalivePeriod) {
		this.keepalivePeriod = keepalivePeriod;
		setConfigChanged(true);
	}

	public void setDefaultKeepalivePeriod() {
		this.keepalivePeriod = new TorConfigDefaults().getKeepalivePeriod();
		setConfigChanged(true);
	}

	public String[] getLog() {
		return log;
	}

	public void setLog(String[] log) {
		this.log = log;
		setConfigChanged(true);
	}

	public void setDefaultLog() {
		this.log = new TorConfigDefaults().getLog();
		setConfigChanged(true);
	}

	public int getMaxConn() {
		return maxConn;
	}

	public void setMaxConn(int maxConn) {
		this.maxConn = maxConn;
		setConfigChanged(true);
	}

	public void setDefaultMaxConn() {
		this.maxConn = new TorConfigDefaults().getMaxConn();
		setConfigChanged(true);
	}

	public InetAddress getOutboundBindAddress() {
		return outboundBindAddress;
	}

	public void setOutboundBindAddress(InetAddress outboundBindAddress) {
		this.outboundBindAddress = outboundBindAddress;
		setConfigChanged(true);
	}

	public void setDefaultOutboundBindAddress() {
		this.outboundBindAddress = new TorConfigDefaults().getOutboundBindAddress();
		setConfigChanged(true);
	}

	public String getPidFile() {
		return pidFile;
	}

	public void setPidFile(String pidFile) {
		this.pidFile = pidFile;
		setConfigChanged(true);
	}

	public void setDefaultPidFile() {
		this.pidFile = new TorConfigDefaults().getPidFile();
		setConfigChanged(true);
	}

	public boolean isRunAsDaemon() {
		return runAsDaemon;
	}

	public void setRunAsDaemon(boolean runAsDaemon) {
		this.runAsDaemon = runAsDaemon;
		setConfigChanged(true);
	}

	public void setDefaultRunAsDaemon() {
		this.runAsDaemon = new TorConfigDefaults().isRunAsDaemon();
		setConfigChanged(true);
	}

	public boolean isSafeLogging() {
		return safeLogging;
	}

	public void setSafeLogging(boolean safeLogging) {
		this.safeLogging = safeLogging;
		setConfigChanged(true);
	}

	public void setDefaultSafeLogging() {
		this.safeLogging = new TorConfigDefaults().isSafeLogging();
		setConfigChanged(true);
	}

	public long getStatusFetchPeriod() {
		return statusFetchPeriod;
	}

	public void setStatusFetchPeriod(long statusFetchPeriod) {
		this.statusFetchPeriod = statusFetchPeriod;
		setConfigChanged(true);
	}

	public void setDefaultStatusFetchPeriod() {
		this.statusFetchPeriod = new TorConfigDefaults().getStatusFetchPeriod();
		setConfigChanged(true);
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
		setConfigChanged(true);
	}

	public void setDefaultUser() {
		this.user = new TorConfigDefaults().getUser();
		setConfigChanged(true);
	}

	public boolean isHardwareAccel() {
		return hardwareAccel;
	}

	public void setHardwareAccel(boolean hardwareAccel) {
		this.hardwareAccel = hardwareAccel;
		setConfigChanged(true);
	}

	public void setDefaultHardwareAccel() {
		this.hardwareAccel = new TorConfigDefaults().isHardwareAccel();
		setConfigChanged(true);
	}

	public String getAllowUnverifiedNodes() {
		return allowUnverifiedNodes;
	}

	public void setAllowUnverifiedNodes(String allowUnverifiedNodes) {
		this.allowUnverifiedNodes = allowUnverifiedNodes;
		setConfigChanged(true);
	}

	public void setDefaultAllowUnverifiedNodes() {
		this.allowUnverifiedNodes = new TorConfigDefaults().getAllowUnverifiedNodes();
		setConfigChanged(true);
	}

	public boolean isClientOnly() {
		return clientOnly;
	}

	public void setClientOnly(boolean clientOnly) {
		this.clientOnly = clientOnly;
		setConfigChanged(true);
	}

	public void setDefaultClientOnly() {
		this.clientOnly = new TorConfigDefaults().isClientOnly();
		setConfigChanged(true);
	}

	public String[] getEntryNodes() {
		return entryNodes;
	}

	public void setEntryNodes(String[] entryNodes) {
		this.entryNodes = entryNodes;
		setConfigChanged(true);
	}

	public void setDefaultEntryNodes() {
		this.entryNodes = new TorConfigDefaults().getEntryNodes();
		setConfigChanged(true);
	}

	public String[] getExitNodes() {
		return exitNodes;
	}

	public void setExitNodes(String[] exitNodes) {
		this.exitNodes = exitNodes;
		setConfigChanged(true);
	}

	public void setDefaultExitNodes() {
		this.exitNodes = new TorConfigDefaults().getExitNodes();
		setConfigChanged(true);
	}

	public String[] getExcludeNodes() {
		return excludeNodes;
	}

	public void setExcludeNodes(String[] excludeNodes) {
		this.excludeNodes = excludeNodes;
		setConfigChanged(true);
	}

	public void setDefaultExcludeNodes() {
		this.excludeNodes = new TorConfigDefaults().getExcludeNodes();
		setConfigChanged(true);
	}

	public boolean isStrictExitNodes() {
		return strictExitNodes;
	}

	public void setStrictExitNodes(boolean strictExitNodes) {
		this.strictExitNodes = strictExitNodes;
		setConfigChanged(true);
	}

	public void setDefaultStrictExitNodes() {
		this.strictExitNodes = new TorConfigDefaults().isStrictExitNodes();
		setConfigChanged(true);
	}

	public boolean isStrictEntryNodes() {
		return strictEntryNodes;
	}

	public void setStrictEntryNodes(boolean strictEntryNodes) {
		this.strictEntryNodes = strictEntryNodes;
		setConfigChanged(true);
	}

	public void setDefaultStrictEntryNodes() {
		this.strictEntryNodes = new TorConfigDefaults().isStrictEntryNodes();
		setConfigChanged(true);
	}

	public boolean isFascistFirewall() {
		return fascistFirewall;
	}

	public void setFascistFirewall(boolean fascistFirewall) {
		this.fascistFirewall = fascistFirewall;
		setConfigChanged(true);
	}

	public void setDefaultFascistFirewall() {
		this.fascistFirewall = new TorConfigDefaults().isFascistFirewall();
		setConfigChanged(true);
	}

	public short[] getFirewallPorts() {
		return firewallPorts;
	}

	public void setFirewallPorts(short[] firewallPorts) {
		this.firewallPorts = firewallPorts;
		setConfigChanged(true);
	}

	public void setDefaultFirewallPorts() {
		this.firewallPorts = new TorConfigDefaults().getFirewallPorts();
		setConfigChanged(true);
	}

	public String[] getFirewallIPs() {
		return firewallIPs;
	}

	public void setFirewallIPs(String[] firewallIPs) {
		this.firewallIPs = firewallIPs;
		setConfigChanged(true);
	}

	public void setDefaultFirewallIPs() {
		this.firewallIPs = new TorConfigDefaults().getFirewallIPs();
		setConfigChanged(true);
	}

	public String getReachableAddresses() {
		return reachableAddresses;
	}

	public void setReachableAddresses(String ReachableAddresses) {
		this.reachableAddresses = ReachableAddresses;
		setConfigChanged(true);
	}

	public void setDefaultReachableAddresses() {
		this.reachableAddresses = new TorConfigDefaults().getReachableAddresses();
		setConfigChanged(true);
	}

	public short[] getLongLivedPorts() {
		return longLivedPorts;
	}

	public void setLongLivedPorts(short[] longLivedPorts) {
		this.longLivedPorts = longLivedPorts;
		setConfigChanged(true);
	}

	public void setDefaultLongLivedPorts() {
		this.longLivedPorts = new TorConfigDefaults().getLongLivedPorts();
		setConfigChanged(true);
	}

	public String[] getMapAddress() {
		return mapAddress;
	}

	public void setMapAddress(String[] mapAddress) {
		this.mapAddress = mapAddress;
		setConfigChanged(true);
	}

	public void setDefaultMapAddress() {
		this.mapAddress = new TorConfigDefaults().getMapAddress();
		setConfigChanged(true);
	}

	public long getNewCircuitPeriod() {
		return newCircuitPeriod;
	}

	public void setNewCircuitPeriod(long newCircuitPeriod) {
		this.newCircuitPeriod = newCircuitPeriod;
		setConfigChanged(true);
	}

	public void setDefaultNewCircuitPeriod() {
		this.newCircuitPeriod = new TorConfigDefaults().getNewCircuitPeriod();
		setConfigChanged(true);
	}

	public long getMaxCircuitDirtiness() {
		return maxCircuitDirtiness;
	}

	public void setMaxCircuitDirtiness(long maxCircuitDirtiness) {
		this.maxCircuitDirtiness = maxCircuitDirtiness;
		setConfigChanged(true);
	}

	public void setDefaultMaxCircuitDirtiness() {
		this.maxCircuitDirtiness = new TorConfigDefaults().getMaxCircuitDirtiness();
		setConfigChanged(true);
	}

	public String[] getNodeFamily() {
		return nodeFamily;
	}

	public void setNodeFamily(String[] nodeFamily) {
		this.nodeFamily = nodeFamily;
		setConfigChanged(true);
	}

	public void setDefaultNodeFamily() {
		this.nodeFamily = new TorConfigDefaults().getNodeFamily();
		setConfigChanged(true);
	}

	public String[] getRendNodes() {
		return rendNodes;
	}

	public void setRendNodes(String[] rendNodes) {
		this.rendNodes = rendNodes;
		setConfigChanged(true);
	}

	public void setDefaultRendNodes() {
		this.rendNodes = new TorConfigDefaults().getRendNodes();
		setConfigChanged(true);
	}

	public String[] getRendExcludeNodes() {
		return rendExcludeNodes;
	}

	public void setRendExcludeNodes(String[] rendExcludeNodes) {
		this.rendExcludeNodes = rendExcludeNodes;
		setConfigChanged(true);
	}

	public void setDefaultRendExcludeNodes() {
		this.rendExcludeNodes = new TorConfigDefaults().getRendExcludeNodes();
		setConfigChanged(true);
	}

	public short getSocksPort() {
		return socksPort;
	}

	public void setSocksPort(short socksPort) {
		this.socksPort = socksPort;
		setConfigChanged(true);
	}

	public void setDefaultSocksPort() {
		this.socksPort = new TorConfigDefaults().getSocksPort();
		setConfigChanged(true);
	}

	public String getSocksBindAddress() {
		return socksBindAddress;
	}

	public void setSocksBindAddress(String socksBindAddress) {
		this.socksBindAddress = socksBindAddress;
		setConfigChanged(true);
	}

	public void setDefaultSocksBindAddress() {
		this.socksBindAddress = new TorConfigDefaults().getSocksBindAddress();
		setConfigChanged(true);
	}

	public String getSocksPolicy() {
		return socksPolicy;
	}

	public void setSocksPolicy(String socksPolicy) {
		this.socksPolicy = socksPolicy;
		setConfigChanged(true);
	}

	public void setDefaultSocksPolicy() {
		this.socksPolicy = new TorConfigDefaults().getSocksPolicy();
		setConfigChanged(true);
	}

	public String[] getTrackHostExits() {
		return trackHostExits;
	}

	public void setTrackHostExits(String[] trackHostExits) {
		this.trackHostExits = trackHostExits;
		setConfigChanged(true);
	}

	public void setDefaultTrackHostExits() {
		this.trackHostExits = new TorConfigDefaults().getTrackHostExits();
		setConfigChanged(true);
	}

	public long getTrackHostExitsExpire() {
		return trackHostExitsExpire;
	}

	public void setTrackHostExitsExpire(long trackHostExitsExpire) {
		this.trackHostExitsExpire = trackHostExitsExpire;
		setConfigChanged(true);
	}

	public void setDefaultTrackHostExitsExpire() {
		this.trackHostExitsExpire = new TorConfigDefaults().getTrackHostExitsExpire();
		setConfigChanged(true);
	}

	public boolean isUseHelperNodes() {
		return useHelperNodes;
	}

	public void setUseHelperNodes(boolean useHelperNodes) {
		this.useHelperNodes = useHelperNodes;
		setConfigChanged(true);
	}

	public void setDefaultUseHelperNodes() {
		this.useHelperNodes = new TorConfigDefaults().isUseHelperNodes();
		setConfigChanged(true);
	}

	public int getNumHelperNodes() {
		return numHelperNodes;
	}

	public void setNumHelperNodes(int numHelperNodes) {
		this.numHelperNodes = numHelperNodes;
		setConfigChanged(true);
	}

	public void setDefaultNumHelperNodes() {
		this.numHelperNodes = new TorConfigDefaults().getNumHelperNodes();
		setConfigChanged(true);
	}

	public String[] getHiddenServiceDir() {
		return hiddenServiceDir;
	}

	public void setHiddenServiceDir(String[] hiddenServiceDir) {
		this.hiddenServiceDir = hiddenServiceDir;
		setConfigChanged(true);
	}

	public void setDefaultHiddenServiceDir() {
		this.hiddenServiceDir = new TorConfigDefaults().getHiddenServiceDir();
		setConfigChanged(true);
	}

	public String[] getHiddenServicePort() {
		return hiddenServicePort;
	}

	public void setHiddenServicePort(String[] hiddenServicePort) {
		this.hiddenServicePort = hiddenServicePort;
		setConfigChanged(true);
	}

	public void setDefaultHiddenServicePort() {
		this.hiddenServicePort = new TorConfigDefaults().getHiddenServicePort();
		setConfigChanged(true);
	}

	public String[] getHiddenServiceNodes() {
		return hiddenServiceNodes;
	}

	public void setHiddenServiceNodes(String[] hiddenServiceNodes) {
		this.hiddenServiceNodes = hiddenServiceNodes;
		setConfigChanged(true);
	}

	public void setDefaultHiddenServiceNodes() {
		this.hiddenServiceNodes = new TorConfigDefaults().getHiddenServiceNodes();
		setConfigChanged(true);
	}

	public String[] getHiddenServiceExcludeNodes() {
		return hiddenServiceExcludeNodes;
	}

	public void setHiddenServiceExcludeNodes(String[] hiddenServiceExcludeNodes) {
		this.hiddenServiceExcludeNodes = hiddenServiceExcludeNodes;
		setConfigChanged(true);
	}

	public void setDefaultHiddenServiceExcludeNodes() {
		this.hiddenServiceExcludeNodes = new TorConfigDefaults().getHiddenServiceExcludeNodes();
		setConfigChanged(true);
	}

	public String getHiddenServiceVersion() {
		return hiddenServiceVersion;
	}

	public void setHiddenServiceVersion(String hiddenServiceVersion) {
		this.hiddenServiceVersion = hiddenServiceVersion;
		setConfigChanged(true);
	}

	public void setDefaultHiddenServiceVersion() {
		this.hiddenServiceVersion = new TorConfigDefaults().getHiddenServiceVersion();
		setConfigChanged(true);
	}

	public long getRendPostPeriod() {
		return rendPostPeriod;
	}

	public void setRendPostPeriod(long rendPostPeriod) {
		this.rendPostPeriod = rendPostPeriod;
		setConfigChanged(true);
	}

	public void setDefaultRendPostPeriod() {
		this.rendPostPeriod = new TorConfigDefaults().getRendPostPeriod();
		setConfigChanged(true);
	}

	public void setDataDirectory(File dataDirectory) {
		this.dataDirectory = dataDirectory;
		setConfigChanged(true);
	}

	public void setDefaultDataDirectory() {
		this.dataDirectory = new TorConfigDefaults().getDataDirectory();
		setConfigChanged(true);
	}

	public void registerConfigChangedHandler(EventHandler eh) {
		configChangedManager.addListener(eh);
	}

	public void unregisterConfigChangedHandler(EventHandler eh) {
		configChangedManager.removeListener(eh);
	}

	public boolean isConfigChanged() {
		synchronized (this) {
			return configChanged;
		}
	}

	public EventManager getConfigChangedManager() {
		return configChangedManager;
	}

	public void setConfigChanged(boolean configChanged) {
		synchronized (this) {
			this.configChanged = configChanged;
		}
	}
}
