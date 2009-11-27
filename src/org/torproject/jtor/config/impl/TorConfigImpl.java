package org.torproject.jtor.config.impl;

import java.io.File;
import java.net.InetAddress;
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

        public TorConfigImpl() {
            createDataDirectory();
        }

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getDataDirectory()
	 */
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
    /* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#loadConf()
	 */
    public void loadConf() {
        TorConfigParserImpl.parseFile(new File(dataDirectory, configFile));
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
    }

    /* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#saveConf()
	 */
    /* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#saveConf()
	 */
    public void saveConf() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void resetConf() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getConfigFile()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getConfigFile()
	 */
	public String getConfigFile() {
		return configFile;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setConfigFile(java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setConfigFile(java.lang.String)
	 */
	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultConfigFile()
	 */
	public void setDefaultConfigFile() {
		this.configFile = new TorConfigDefaults().getConfigFile();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getBandwidthRate()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getBandwidthRate()
	 */
	public long getBandwidthRate() {
		return bandwidthRate;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setBandwidthRate(long)
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setBandwidthRate(long)
	 */
	public void setBandwidthRate(long bandwidthRate) {
		this.bandwidthRate = bandwidthRate;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultBandwidthRate()
	 */
	public void setDefaultBandwidthRate() {
		this.bandwidthRate = new TorConfigDefaults().getBandwidthRate();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getBandwidthBurst()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getBandwidthBurst()
	 */
	public long getBandwidthBurst() {
		return bandwidthBurst;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setBandwidthBurst(long)
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setBandwidthBurst(long)
	 */
	public void setBandwidthBurst(long bandwidthBurst) {
		this.bandwidthBurst = bandwidthBurst;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultBandwidthBurst()
	 */
	public void setDefaultBandwidthBurst() {
		this.bandwidthBurst = new TorConfigDefaults().getBandwidthBurst();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getMaxAdvertisedBandwidth()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getMaxAdvertisedBandwidth()
	 */
	public long getMaxAdvertisedBandwidth() {
		return maxAdvertisedBandwidth;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setMaxAdvertisedBandwidth(long)
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setMaxAdvertisedBandwidth(long)
	 */
	public void setMaxAdvertisedBandwidth(long maxAdvertisedBandwidth) {
		this.maxAdvertisedBandwidth = maxAdvertisedBandwidth;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultMaxAdvertisedBandwidth()
	 */
	public void setDefaultMaxAdvertisedBandwidth() {
		this.maxAdvertisedBandwidth = new TorConfigDefaults().getMaxAdvertisedBandwidth();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getControlPort()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getControlPort()
	 */
	public short getControlPort() {
		return controlPort;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setControlPort(short)
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setControlPort(short)
	 */
	public void setControlPort(short controlPort) {
		this.controlPort = controlPort;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultControlPort()
	 */
	public void setDefaultControlPort() {
		this.controlPort = new TorConfigDefaults().getControlPort();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getHashedControlPassword()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getHashedControlPassword()
	 */
	public String getHashedControlPassword() {
		return hashedControlPassword;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setHashedControlPassword(java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setHashedControlPassword(java.lang.String)
	 */
	public void setHashedControlPassword(String hashedControlPassword) {
		this.hashedControlPassword = hashedControlPassword;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultHashedControlPassword()
	 */
	public void setDefaultHashedControlPassword() {
		this.hashedControlPassword = new TorConfigDefaults().getHashedControlPassword();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#isCookieAuthentication()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#isCookieAuthentication()
	 */
	public boolean isCookieAuthentication() {
		return cookieAuthentication;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setCookieAuthentication(boolean)
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setCookieAuthentication(boolean)
	 */
	public void setCookieAuthentication(boolean cookieAuthentication) {
		this.cookieAuthentication = cookieAuthentication;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultCookieAuthentication()
	 */
	public void setDefaultCookieAuthentication() {
		this.cookieAuthentication = new TorConfigDefaults().isCookieAuthentication();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getDirFetchPeriod()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getDirFetchPeriod()
	 */
	public long getDirFetchPeriod() {
		return dirFetchPeriod;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDirFetchPeriod(long)
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDirFetchPeriod(long)
	 */
	public void setDirFetchPeriod(long dirFetchPeriod) {
		this.dirFetchPeriod = dirFetchPeriod;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultDirFetchPeriod()
	 */
	public void setDefaultDirFetchPeriod() {
		this.dirFetchPeriod = new TorConfigDefaults().getDirFetchPeriod();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getDirServer()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getDirServer()
	 */
	public String[] getDirServer() {
		return dirServer;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDirServer(java.lang.String[])
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDirServer(java.lang.String[])
	 */
	public void setDirServer(String[] dirServer) {
		this.dirServer = dirServer;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultDirServer()
	 */
	public void setDefaultDirServer() {
		this.dirServer = new TorConfigDefaults().getDirServer();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#isDisableAllSwap()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#isDisableAllSwap()
	 */
	public boolean isDisableAllSwap() {
		return disableAllSwap;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDisableAllSwap(boolean)
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDisableAllSwap(boolean)
	 */
	public void setDisableAllSwap(boolean disableAllSwap) {
		this.disableAllSwap = disableAllSwap;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDisableAllSwap()
	 */
	public void setDisableAllSwap() {
		this.disableAllSwap = new TorConfigDefaults().isDisableAllSwap();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getGroup()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getGroup()
	 */
	public String getGroup() {
		return group;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setGroup(java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setGroup(java.lang.String)
	 */
	public void setGroup(String group) {
		this.group = group;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultGroup()
	 */
	public void setDefaultGroup() {
		this.group = new TorConfigDefaults().getGroup();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getHttpProxy()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getHttpProxy()
	 */
	public String getHttpProxy() {
		return httpProxy;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setHttpProxy(java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setHttpProxy(java.lang.String)
	 */
	public void setHttpProxy(String httpProxy) {
		this.httpProxy = httpProxy;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultHttpProxy()
	 */
	public void setDefaultHttpProxy() {
		this.httpProxy = new TorConfigDefaults().getHttpProxy();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getHttpProxyAuthenticator()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getHttpProxyAuthenticator()
	 */
	public String getHttpProxyAuthenticator() {
		return httpProxyAuthenticator;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setHttpProxyAuthenticator(java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setHttpProxyAuthenticator(java.lang.String)
	 */
	public void setHttpProxyAuthenticator(String httpProxyAuthenticator) {
		this.httpProxyAuthenticator = httpProxyAuthenticator;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultHttpProxyAuthenticator()
	 */
	public void setDefaultHttpProxyAuthenticator() {
		this.httpProxyAuthenticator = new TorConfigDefaults().getHttpProxyAuthenticator();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getHttpsProxy()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getHttpsProxy()
	 */
	public String getHttpsProxy() {
		return httpsProxy;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setHttpsProxy(java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setHttpsProxy(java.lang.String)
	 */
	public void setHttpsProxy(String httpsProxy) {
		this.httpsProxy = httpsProxy;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultHttpsProxy()
	 */
	public void setDefaultHttpsProxy() {
		this.httpsProxy = new TorConfigDefaults().getHttpsProxy();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getHttpsProxyAuthenticator()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getHttpsProxyAuthenticator()
	 */
	public String getHttpsProxyAuthenticator() {
		return httpsProxyAuthenticator;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setHttpsProxyAuthenticator(java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setHttpsProxyAuthenticator(java.lang.String)
	 */
	public void setHttpsProxyAuthenticator(String httpsProxyAuthenticator) {
		this.httpsProxyAuthenticator = httpsProxyAuthenticator;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultHttpsProxyAuthenticator()
	 */
	public void setDefaultHttpsProxyAuthenticator() {
		this.httpsProxyAuthenticator = new TorConfigDefaults().getHttpsProxyAuthenticator();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getKeepalivePeriod()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getKeepalivePeriod()
	 */
	public int getKeepalivePeriod() {
		return keepalivePeriod;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setKeepalivePeriod(int)
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setKeepalivePeriod(int)
	 */
	public void setKeepalivePeriod(int keepalivePeriod) {
		this.keepalivePeriod = keepalivePeriod;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultKeepalivePeriod()
	 */
	public void setDefaultKeepalivePeriod() {
		this.keepalivePeriod = new TorConfigDefaults().getKeepalivePeriod();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getLog()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getLog()
	 */
	public String[] getLog() {
		return log;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setLog(java.lang.String[])
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setLog(java.lang.String[])
	 */
	public void setLog(String[] log) {
		this.log = log;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultLog()
	 */
	public void setDefaultLog() {
		this.log = new TorConfigDefaults().getLog();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getMaxConn()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getMaxConn()
	 */
	public int getMaxConn() {
		return maxConn;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setMaxConn(int)
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setMaxConn(int)
	 */
	public void setMaxConn(int maxConn) {
		this.maxConn = maxConn;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultMaxConn()
	 */
	public void setDefaultMaxConn() {
		this.maxConn = new TorConfigDefaults().getMaxConn();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getOutboundBindAddress()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getOutboundBindAddress()
	 */
	public InetAddress getOutboundBindAddress() {
		return outboundBindAddress;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setOutboundBindAddress(java.net.InetAddress)
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setOutboundBindAddress(java.net.InetAddress)
	 */
	public void setOutboundBindAddress(InetAddress outboundBindAddress) {
		this.outboundBindAddress = outboundBindAddress;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultOutboundBindAddress()
	 */
	public void setDefaultOutboundBindAddress() {
		this.outboundBindAddress = new TorConfigDefaults().getOutboundBindAddress();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getPidFile()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getPidFile()
	 */
	public String getPidFile() {
		return pidFile;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setPidFile(java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setPidFile(java.lang.String)
	 */
	public void setPidFile(String pidFile) {
		this.pidFile = pidFile;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultPidFile()
	 */
	public void setDefaultPidFile() {
		this.pidFile = new TorConfigDefaults().getPidFile();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#isRunAsDaemon()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#isRunAsDaemon()
	 */
	public boolean isRunAsDaemon() {
		return runAsDaemon;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setRunAsDaemon(boolean)
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setRunAsDaemon(boolean)
	 */
	public void setRunAsDaemon(boolean runAsDaemon) {
		this.runAsDaemon = runAsDaemon;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultRunAsDaemon()
	 */
	public void setDefaultRunAsDaemon() {
		this.runAsDaemon = new TorConfigDefaults().isRunAsDaemon();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#isSafeLogging()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#isSafeLogging()
	 */
	public boolean isSafeLogging() {
		return safeLogging;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setSafeLogging(boolean)
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setSafeLogging(boolean)
	 */
	public void setSafeLogging(boolean safeLogging) {
		this.safeLogging = safeLogging;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultSafeLogging()
	 */
	public void setDefaultSafeLogging() {
		this.safeLogging = new TorConfigDefaults().isSafeLogging();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getStatusFetchPeriod()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getStatusFetchPeriod()
	 */
	public long getStatusFetchPeriod() {
		return statusFetchPeriod;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setStatusFetchPeriod(long)
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setStatusFetchPeriod(long)
	 */
	public void setStatusFetchPeriod(long statusFetchPeriod) {
		this.statusFetchPeriod = statusFetchPeriod;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultStatusFetchPeriod()
	 */
	public void setDefaultStatusFetchPeriod() {
		this.statusFetchPeriod = new TorConfigDefaults().getStatusFetchPeriod();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getUser()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getUser()
	 */
	public String getUser() {
		return user;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setUser(java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setUser(java.lang.String)
	 */
	public void setUser(String user) {
		this.user = user;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultUser()
	 */
	public void setDefaultUser() {
		this.user = new TorConfigDefaults().getUser();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#isHardwareAccel()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#isHardwareAccel()
	 */
	public boolean isHardwareAccel() {
		return hardwareAccel;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setHardwareAccel(boolean)
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setHardwareAccel(boolean)
	 */
	public void setHardwareAccel(boolean hardwareAccel) {
		this.hardwareAccel = hardwareAccel;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultHardwareAccel()
	 */
	public void setDefaultHardwareAccel() {
		this.hardwareAccel = new TorConfigDefaults().isHardwareAccel();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getAllowUnverifiedNodes()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getAllowUnverifiedNodes()
	 */
	public String getAllowUnverifiedNodes() {
		return allowUnverifiedNodes;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setAllowUnverifiedNodes(java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setAllowUnverifiedNodes(java.lang.String)
	 */
	public void setAllowUnverifiedNodes(String allowUnverifiedNodes) {
		this.allowUnverifiedNodes = allowUnverifiedNodes;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultAllowUnverifiedNodes()
	 */
	public void setDefaultAllowUnverifiedNodes() {
		this.allowUnverifiedNodes = new TorConfigDefaults().getAllowUnverifiedNodes();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#isClientOnly()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#isClientOnly()
	 */
	public boolean isClientOnly() {
		return clientOnly;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setClientOnly(boolean)
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setClientOnly(boolean)
	 */
	public void setClientOnly(boolean clientOnly) {
		this.clientOnly = clientOnly;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultClientOnly()
	 */
	public void setDefaultClientOnly() {
		this.clientOnly = new TorConfigDefaults().isClientOnly();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getEntryNodes()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getEntryNodes()
	 */
	public String[] getEntryNodes() {
		return entryNodes;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setEntryNodes(java.lang.String[])
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setEntryNodes(java.lang.String[])
	 */
	public void setEntryNodes(String[] entryNodes) {
		this.entryNodes = entryNodes;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultEntryNodes()
	 */
	public void setDefaultEntryNodes() {
		this.entryNodes = new TorConfigDefaults().getEntryNodes();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getExitNodes()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getExitNodes()
	 */
	public String[] getExitNodes() {
		return exitNodes;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setExitNodes(java.lang.String[])
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setExitNodes(java.lang.String[])
	 */
	public void setExitNodes(String[] exitNodes) {
		this.exitNodes = exitNodes;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultExitNodes()
	 */
	public void setDefaultExitNodes() {
		this.exitNodes = new TorConfigDefaults().getExitNodes();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getExcludeNodes()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getExcludeNodes()
	 */
	public String[] getExcludeNodes() {
		return excludeNodes;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setExcludeNodes(java.lang.String[])
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setExcludeNodes(java.lang.String[])
	 */
	public void setExcludeNodes(String[] excludeNodes) {
		this.excludeNodes = excludeNodes;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultExcludeNodes()
	 */
	public void setDefaultExcludeNodes() {
		this.excludeNodes = new TorConfigDefaults().getExcludeNodes();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#isStrictExitNodes()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#isStrictExitNodes()
	 */
	public boolean isStrictExitNodes() {
		return strictExitNodes;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setStrictExitNodes(boolean)
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setStrictExitNodes(boolean)
	 */
	public void setStrictExitNodes(boolean strictExitNodes) {
		this.strictExitNodes = strictExitNodes;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultStrictExitNodes()
	 */
	public void setDefaultStrictExitNodes() {
		this.strictExitNodes = new TorConfigDefaults().isStrictExitNodes();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#isStrictEntryNodes()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#isStrictEntryNodes()
	 */
	public boolean isStrictEntryNodes() {
		return strictEntryNodes;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setStrictEntryNodes(boolean)
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setStrictEntryNodes(boolean)
	 */
	public void setStrictEntryNodes(boolean strictEntryNodes) {
		this.strictEntryNodes = strictEntryNodes;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultStrictEntryNodes()
	 */
	public void setDefaultStrictEntryNodes() {
		this.strictEntryNodes = new TorConfigDefaults().isStrictEntryNodes();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#isFascistFirewall()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#isFascistFirewall()
	 */
	public boolean isFascistFirewall() {
		return fascistFirewall;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setFascistFirewall(boolean)
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setFascistFirewall(boolean)
	 */
	public void setFascistFirewall(boolean fascistFirewall) {
		this.fascistFirewall = fascistFirewall;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultFascistFirewall()
	 */
	public void setDefaultFascistFirewall() {
		this.fascistFirewall = new TorConfigDefaults().isFascistFirewall();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getFirewallPorts()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getFirewallPorts()
	 */
	public short[] getFirewallPorts() {
		return firewallPorts;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setFirewallPorts(short[])
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setFirewallPorts(short[])
	 */
	public void setFirewallPorts(short[] firewallPorts) {
		this.firewallPorts = firewallPorts;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultFirewallPorts()
	 */
	public void setDefaultFirewallPorts() {
		this.firewallPorts = new TorConfigDefaults().getFirewallPorts();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getFirewallIPs()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getFirewallIPs()
	 */
	public String[] getFirewallIPs() {
		return firewallIPs;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setFirewallIPs(java.lang.String[])
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setFirewallIPs(java.lang.String[])
	 */
	public void setFirewallIPs(String[] firewallIPs) {
		this.firewallIPs = firewallIPs;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultFirewallIPs()
	 */
	public void setDefaultFirewallIPs() {
		this.firewallIPs = new TorConfigDefaults().getFirewallIPs();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getLongLivedPorts()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getLongLivedPorts()
	 */
	public short[] getLongLivedPorts() {
		return longLivedPorts;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setLongLivedPorts(short[])
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setLongLivedPorts(short[])
	 */
	public void setLongLivedPorts(short[] longLivedPorts) {
		this.longLivedPorts = longLivedPorts;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultLongLivedPorts()
	 */
	public void setDefaultLongLivedPorts() {
		this.longLivedPorts = new TorConfigDefaults().getLongLivedPorts();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getMapAddress()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getMapAddress()
	 */
	public String[] getMapAddress() {
		return mapAddress;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setMapAddress(java.lang.String[])
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setMapAddress(java.lang.String[])
	 */
	public void setMapAddress(String[] mapAddress) {
		this.mapAddress = mapAddress;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultMapAddress()
	 */
	public void setDefaultMapAddress() {
		this.mapAddress = new TorConfigDefaults().getMapAddress();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getNewCircuitPeriod()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getNewCircuitPeriod()
	 */
	public long getNewCircuitPeriod() {
		return newCircuitPeriod;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setNewCircuitPeriod(long)
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setNewCircuitPeriod(long)
	 */
	public void setNewCircuitPeriod(long newCircuitPeriod) {
		this.newCircuitPeriod = newCircuitPeriod;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultNewCircuitPeriod()
	 */
	public void setDefaultNewCircuitPeriod() {
		this.newCircuitPeriod = new TorConfigDefaults().getNewCircuitPeriod();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getMaxCircuitDirtiness()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getMaxCircuitDirtiness()
	 */
	public long getMaxCircuitDirtiness() {
		return maxCircuitDirtiness;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setMaxCircuitDirtiness(long)
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setMaxCircuitDirtiness(long)
	 */
	public void setMaxCircuitDirtiness(long maxCircuitDirtiness) {
		this.maxCircuitDirtiness = maxCircuitDirtiness;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultMaxCircuitDirtiness()
	 */
	public void setDefaultMaxCircuitDirtiness() {
		this.maxCircuitDirtiness = new TorConfigDefaults().getMaxCircuitDirtiness();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getNodeFamily()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getNodeFamily()
	 */
	public String[] getNodeFamily() {
		return nodeFamily;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setNodeFamily(java.lang.String[])
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setNodeFamily(java.lang.String[])
	 */
	public void setNodeFamily(String[] nodeFamily) {
		this.nodeFamily = nodeFamily;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultNodeFamily()
	 */
	public void setDefaultNodeFamily() {
		this.nodeFamily = new TorConfigDefaults().getNodeFamily();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getRendNodes()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getRendNodes()
	 */
	public String[] getRendNodes() {
		return rendNodes;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setRendNodes(java.lang.String[])
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setRendNodes(java.lang.String[])
	 */
	public void setRendNodes(String[] rendNodes) {
		this.rendNodes = rendNodes;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultRendNodes()
	 */
	public void setDefaultRendNodes() {
		this.rendNodes = new TorConfigDefaults().getRendNodes();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getRendExcludeNodes()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getRendExcludeNodes()
	 */
	public String[] getRendExcludeNodes() {
		return rendExcludeNodes;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setRendExcludeNodes(java.lang.String[])
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setRendExcludeNodes(java.lang.String[])
	 */
	public void setRendExcludeNodes(String[] rendExcludeNodes) {
		this.rendExcludeNodes = rendExcludeNodes;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultRendExcludeNodes()
	 */
	public void setDefaultRendExcludeNodes() {
		this.rendExcludeNodes = new TorConfigDefaults().getRendExcludeNodes();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getSocksPort()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getSocksPort()
	 */
	public short getSocksPort() {
		return socksPort;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setSocksPort(short)
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setSocksPort(short)
	 */
	public void setSocksPort(short socksPort) {
		this.socksPort = socksPort;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultSocksPort()
	 */
	public void setDefaultSocksPort() {
		this.socksPort = new TorConfigDefaults().getSocksPort();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getSocksBindAddress()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getSocksBindAddress()
	 */
	public String getSocksBindAddress() {
		return socksBindAddress;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setSocksBindAddress(java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setSocksBindAddress(java.lang.String)
	 */
	public void setSocksBindAddress(String socksBindAddress) {
		this.socksBindAddress = socksBindAddress;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultSocksBindAddress()
	 */
	public void setDefaultSocksBindAddress() {
		this.socksBindAddress = new TorConfigDefaults().getSocksBindAddress();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getSocksPolicy()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getSocksPolicy()
	 */
	public String getSocksPolicy() {
		return socksPolicy;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setSocksPolicy(java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setSocksPolicy(java.lang.String)
	 */
	public void setSocksPolicy(String socksPolicy) {
		this.socksPolicy = socksPolicy;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultSocksPolicy()
	 */
	public void setDefaultSocksPolicy() {
		this.socksPolicy = new TorConfigDefaults().getSocksPolicy();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getTrackHostExits()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getTrackHostExits()
	 */
	public String[] getTrackHostExits() {
		return trackHostExits;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setTrackHostExits(java.lang.String[])
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setTrackHostExits(java.lang.String[])
	 */
	public void setTrackHostExits(String[] trackHostExits) {
		this.trackHostExits = trackHostExits;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultTrackHostExits()
	 */
	public void setDefaultTrackHostExits() {
		this.trackHostExits = new TorConfigDefaults().getTrackHostExits();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getTrackHostExitsExpire()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getTrackHostExitsExpire()
	 */
	public long getTrackHostExitsExpire() {
		return trackHostExitsExpire;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setTrackHostExitsExpire(long)
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setTrackHostExitsExpire(long)
	 */
	public void setTrackHostExitsExpire(long trackHostExitsExpire) {
		this.trackHostExitsExpire = trackHostExitsExpire;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultTrackHostExitsExpire()
	 */
	public void setDefaultTrackHostExitsExpire() {
		this.trackHostExitsExpire = new TorConfigDefaults().getTrackHostExitsExpire();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#isUseHelperNodes()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#isUseHelperNodes()
	 */
	public boolean isUseHelperNodes() {
		return useHelperNodes;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setUseHelperNodes(boolean)
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setUseHelperNodes(boolean)
	 */
	public void setUseHelperNodes(boolean useHelperNodes) {
		this.useHelperNodes = useHelperNodes;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultUseHelperNodes()
	 */
	public void setDefaultUseHelperNodes() {
		this.useHelperNodes = new TorConfigDefaults().isUseHelperNodes();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getNumHelperNodes()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getNumHelperNodes()
	 */
	public int getNumHelperNodes() {
		return numHelperNodes;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setNumHelperNodes(int)
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setNumHelperNodes(int)
	 */
	public void setNumHelperNodes(int numHelperNodes) {
		this.numHelperNodes = numHelperNodes;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultNumHelperNodes()
	 */
	public void setDefaultNumHelperNodes() {
		this.numHelperNodes = new TorConfigDefaults().getNumHelperNodes();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getHiddenServiceDir()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getHiddenServiceDir()
	 */
	public String[] getHiddenServiceDir() {
		return hiddenServiceDir;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setHiddenServiceDir(java.lang.String[])
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setHiddenServiceDir(java.lang.String[])
	 */
	public void setHiddenServiceDir(String[] hiddenServiceDir) {
		this.hiddenServiceDir = hiddenServiceDir;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultHiddenServiceDir()
	 */
	public void setDefaultHiddenServiceDir() {
		this.hiddenServiceDir = new TorConfigDefaults().getHiddenServiceDir();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getHiddenServicePort()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getHiddenServicePort()
	 */
	public String[] getHiddenServicePort() {
		return hiddenServicePort;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setHiddenServicePort(java.lang.String[])
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setHiddenServicePort(java.lang.String[])
	 */
	public void setHiddenServicePort(String[] hiddenServicePort) {
		this.hiddenServicePort = hiddenServicePort;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultHiddenServicePort()
	 */
	public void setDefaultHiddenServicePort() {
		this.hiddenServicePort = new TorConfigDefaults().getHiddenServicePort();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getHiddenServiceNodes()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getHiddenServiceNodes()
	 */
	public String[] getHiddenServiceNodes() {
		return hiddenServiceNodes;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setHiddenServiceNodes(java.lang.String[])
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setHiddenServiceNodes(java.lang.String[])
	 */
	public void setHiddenServiceNodes(String[] hiddenServiceNodes) {
		this.hiddenServiceNodes = hiddenServiceNodes;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultHiddenServiceNodes()
	 */
	public void setDefaultHiddenServiceNodes() {
		this.hiddenServiceNodes = new TorConfigDefaults().getHiddenServiceNodes();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getHiddenServiceExcludeNodes()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getHiddenServiceExcludeNodes()
	 */
	public String[] getHiddenServiceExcludeNodes() {
		return hiddenServiceExcludeNodes;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setHiddenServiceExcludeNodes(java.lang.String[])
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setHiddenServiceExcludeNodes(java.lang.String[])
	 */
	public void setHiddenServiceExcludeNodes(String[] hiddenServiceExcludeNodes) {
		this.hiddenServiceExcludeNodes = hiddenServiceExcludeNodes;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultHiddenServiceExcludeNodes()
	 */
	public void setDefaultHiddenServiceExcludeNodes() {
		this.hiddenServiceExcludeNodes = new TorConfigDefaults().getHiddenServiceExcludeNodes();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getHiddenServiceVersion()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getHiddenServiceVersion()
	 */
	public String getHiddenServiceVersion() {
		return hiddenServiceVersion;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setHiddenServiceVersion(java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setHiddenServiceVersion(java.lang.String)
	 */
	public void setHiddenServiceVersion(String hiddenServiceVersion) {
		this.hiddenServiceVersion = hiddenServiceVersion;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultHiddenServiceVersion()
	 */
	public void setDefaultHiddenServiceVersion() {
		this.hiddenServiceVersion = new TorConfigDefaults().getHiddenServiceVersion();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getRendPostPeriod()
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#getRendPostPeriod()
	 */
	public long getRendPostPeriod() {
		return rendPostPeriod;
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setRendPostPeriod(long)
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setRendPostPeriod(long)
	 */
	public void setRendPostPeriod(long rendPostPeriod) {
		this.rendPostPeriod = rendPostPeriod;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultRendPostPeriod()
	 */
	public void setDefaultRendPostPeriod() {
		this.rendPostPeriod = new TorConfigDefaults().getRendPostPeriod();
	}

	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDataDirectory(java.io.File)
	 */
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDataDirectory(java.io.File)
	 */
	public void setDataDirectory(File dataDirectory) {
		this.dataDirectory = dataDirectory;
	}
	
	/* (non-Javadoc)
	 * @see org.torproject.jtor.config.impl.TorConfig#setDefaultDataDirectory()
	 */
	public void setDefaultDataDirectory() {
		this.dataDirectory = new TorConfigDefaults().getDataDirectory();
	}
}
