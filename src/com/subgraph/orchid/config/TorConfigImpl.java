package com.subgraph.orchid.config;

import com.subgraph.orchid.TorConfig;
import com.subgraph.orchid.circuits.hs.HSDescriptorCookie;
import com.subgraph.orchid.data.HexDigest;
import com.subgraph.orchid.data.IPv4Address;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TorConfigImpl implements TorConfig {

    private void resetDefaults() {
        dataDirectory = toFile("~/.orchid");
        circuitBuildTimeout = toMS(60, TimeUnit.SECONDS);
        circuitStreamTimeout = 0;
        circuitIdleTimeout = toMS(1, TimeUnit.HOURS);
        newCircuitPeriod = toMS(30, TimeUnit.SECONDS);
        maxCircuitDirtiness = toMS(10, TimeUnit.MINUTES);
        maxClientCircuitsPending = 32;
        enforceDistinctSubnets = true;
        socksTimeout = toMS(2, TimeUnit.MINUTES);
        numEntryGuards = 3;
        useEntryGuards = true;
        longLivedPorts = toIntList("21,22,706,1863,5050,5190,5222,5223,6523,6667,6697,8300");
        excludedNodes = Collections.emptyList();
        excludedExitNodes = Collections.emptyList();
        exitNodes = Collections.emptyList();
        entryNodes = Collections.emptyList();
        strictNodes = false;
        fascistFirewall = false;
        firewallPorts = toIntList("80,443");
        safeSocks = false;
        safeLogging = true;
        warnUnsafeSocks = true;
        clientRejectInternalAddress = true;
        handshakeV3Enabled = true;
        handshakeV2Enabled = true;
        hsAuth = new TorConfigHSAuth();
        useNtorHandshake = AutoBoolValue.AUTO;
        useMicrodescriptors = AutoBoolValue.AUTO;
        useBridges = false;
        bridgeLines = new ArrayList<TorConfigBridgeLine>();
    }

    private File dataDirectory;
    private long circuitBuildTimeout;
    private long circuitStreamTimeout;
    private long circuitIdleTimeout;
    private long newCircuitPeriod;
    private long maxCircuitDirtiness;
    private int maxClientCircuitsPending;
    private boolean enforceDistinctSubnets;
    private long socksTimeout;
    private int numEntryGuards;
    private boolean useEntryGuards;
    private List<Integer> longLivedPorts;
    private List<String> excludedNodes;
    private List<String> excludedExitNodes;
    private List<String> exitNodes;
    private List<String> entryNodes;
    private boolean strictNodes;
    private boolean fascistFirewall;
    private List<Integer> firewallPorts;
    private boolean safeSocks;
    private boolean safeLogging;
    private boolean warnUnsafeSocks;
    private boolean clientRejectInternalAddress;
    private boolean handshakeV3Enabled;
    private boolean handshakeV2Enabled;
    private TorConfigHSAuth hsAuth;
    private AutoBoolValue useNtorHandshake;
    private AutoBoolValue useMicrodescriptors;
    private boolean useBridges;
    private List<TorConfigBridgeLine> bridgeLines;


    private static long toMS(long time, TimeUnit unit) {
        return TimeUnit.MILLISECONDS.convert(time, unit);
    }

    private static File toFile(String path) {
        if(path.startsWith("~/")) {
            final File home = new File(System.getProperty("user.home"));
            return new File(home, path.substring(2));
        }
        return new File(path);
    }

    private static List<Integer> toIntList(String val) {
        final List<Integer> list = new ArrayList<Integer>();
        for(String s: val.split(",")) {
            list.add(Integer.parseInt(s));
        }
        return list;
    }

    private static List<String> toStringList(String val) {
        final List<String> list = new ArrayList<String>();
        for(String s: val.split(",")) {
            list.add(s);
        }
        return list;
    }

    public TorConfigImpl() {
        resetDefaults();
    }

    @Override
    public File getDataDirectory() {
        return dataDirectory;
    }

    @Override
    public void setDataDirectory(File directory) {
        this.dataDirectory = directory;
    }

    @Override
    public long getCircuitBuildTimeout() {
        return circuitBuildTimeout;
    }

    @Override
    public void setCircuitBuildTimeout(long time, TimeUnit unit) {
        circuitBuildTimeout = toMS(time, unit);
    }

    @Override
    public long getCircuitStreamTimeout() {
        return circuitStreamTimeout;
    }

    @Override
    public void setCircuitStreamTimeout(long time, TimeUnit unit) {
        circuitStreamTimeout = toMS(time, unit);
    }

    @Override
    public long getCircuitIdleTimeout() {
        return circuitIdleTimeout;
    }

    @Override
    public void setCircuitIdleTimeout(long time, TimeUnit unit) {
        circuitIdleTimeout = toMS(time, unit);
    }

    @Override
    public long getNewCircuitPeriod() {
        return newCircuitPeriod;
    }

    @Override
    public void setNewCircuitPeriod(long time, TimeUnit unit) {
        newCircuitPeriod = toMS(time, unit);

    }

    @Override
    public long getMaxCircuitDirtiness() {
        return maxCircuitDirtiness;
    }

    @Override
    public void setMaxCircuitDirtiness(long time, TimeUnit unit) {
        maxCircuitDirtiness = toMS(time, unit);
    }

    @Override
    public int getMaxClientCircuitsPending() {
        return maxClientCircuitsPending;
    }

    @Override
    public void setMaxClientCircuitsPending(int value) {
        maxClientCircuitsPending = value;
    }

    @Override
    public boolean getEnforceDistinctSubnets() {
        return enforceDistinctSubnets;
    }

    @Override
    public void setEnforceDistinctSubnets(boolean value) {
        enforceDistinctSubnets = value;
    }

    @Override
    public long getSocksTimeout() {
        return socksTimeout;
    }

    @Override
    public void setSocksTimeout(long value) {
        socksTimeout = value;
    }

    @Override
    public int getNumEntryGuards() {
        return numEntryGuards;
    }

    @Override
    public void setNumEntryGuards(int value) {
        numEntryGuards = value;
    }

    @Override
    public boolean getUseEntryGuards() {
        return useEntryGuards;
    }

    @Override
    public void setUseEntryGuards(boolean value) {
        useEntryGuards = value;
    }

    @Override
    public List<Integer> getLongLivedPorts() {
        return longLivedPorts;
    }

    @Override
    public void setLongLivedPorts(List<Integer> ports) {
        longLivedPorts = ports;
    }

    @Override
    public List<String> getExcludeNodes() {
        return excludedNodes;
    }

    @Override
    public void setExcludeNodes(List<String> nodes) {
        excludedNodes = nodes;
    }

    @Override
    public List<String> getExcludeExitNodes() {
        return excludedExitNodes;
    }

    @Override
    public void setExcludeExitNodes(List<String> nodes) {
        excludedExitNodes = nodes;
    }

    @Override
    public List<String> getExitNodes() {
        return exitNodes;
    }

    @Override
    public void setExitNodes(List<String> nodes) {
        exitNodes = nodes;
    }

    @Override
    public List<String> getEntryNodes() {
        return entryNodes;
    }

    @Override
    public void setEntryNodes(List<String> nodes) {
        entryNodes = nodes;
    }

    @Override
    public boolean getStrictNodes() {
        return strictNodes;
    }

    @Override
    public void setStrictNodes(boolean value) {
        strictNodes = value;
    }

    @Override
    public boolean getFascistFirewall() {
        return fascistFirewall;
    }

    @Override
    public void setFascistFirewall(boolean value) {
        fascistFirewall = value;
    }

    @Override
    public List<Integer> getFirewallPorts() {
        return firewallPorts;
    }

    @Override
    public void setFirewallPorts(List<Integer> ports) {
        firewallPorts = ports;
    }

    @Override
    public boolean getSafeSocks() {
        return safeSocks;
    }

    @Override
    public void setSafeSocks(boolean value) {
        safeSocks = value;
    }

    @Override
    public boolean getSafeLogging() {
        return safeLogging;
    }

    @Override
    public void setSafeLogging(boolean value) {
        safeLogging = value;
    }

    @Override
    public boolean getWarnUnsafeSocks() {
        return warnUnsafeSocks;
    }

    @Override
    public void setWarnUnsafeSocks(boolean value) {
        warnUnsafeSocks = value;
    }

    @Override
    public boolean getClientRejectInternalAddress() {
        return clientRejectInternalAddress;
    }

    @Override
    public void setClientRejectInternalAddress(boolean value) {
        clientRejectInternalAddress = value;
    }

    @Override
    public boolean getHandshakeV3Enabled() {
        return handshakeV3Enabled;
    }

    @Override
    public void setHandshakeV3Enabled(boolean value) {
        handshakeV3Enabled = value;
    }

    @Override
    public boolean getHandshakeV2Enabled() {
        return handshakeV2Enabled;
    }

    @Override
    public void setHandshakeV2Enabled(boolean value) {
        handshakeV2Enabled = value;
    }

    @Override
    public HSDescriptorCookie getHidServAuth(String key) {
        return hsAuth.get(key);
    }

    @Override
    public void addHidServAuth(String key, String value) {
        hsAuth.add(key, value);
    }

    @Override
    public AutoBoolValue getUseNTorHandshake() {
        return useNtorHandshake;
    }

    @Override
    public void setUseNTorHandshake(AutoBoolValue value) {
        useNtorHandshake = value;
    }

    @Override
    public AutoBoolValue getUseMicrodescriptors() {
        return useMicrodescriptors;
    }

    @Override
    public void setUseMicrodescriptors(AutoBoolValue value) {
        useMicrodescriptors = value;
    }

    @Override
    public boolean getUseBridges() {
        return useBridges;
    }

    @Override
    public void setUseBridges(boolean value) {
        useBridges = value;
    }

    @Override
    public List<TorConfigBridgeLine> getBridges() {
        return bridgeLines;
    }

    @Override
    public void addBridge(IPv4Address address, int port) {
        bridgeLines.add(new TorConfigBridgeLine(address, port, null));
    }

    @Override
    public void addBridge(IPv4Address address, int port, HexDigest fingerprint) {
        bridgeLines.add(new TorConfigBridgeLine(address, port, fingerprint));
    }
}
