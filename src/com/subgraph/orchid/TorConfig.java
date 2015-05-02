package com.subgraph.orchid;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.subgraph.orchid.circuits.hs.HSDescriptorCookie;
import com.subgraph.orchid.config.TorConfigBridgeLine;
import com.subgraph.orchid.data.HexDigest;
import com.subgraph.orchid.data.IPv4Address;


public interface TorConfig {
	
	File getDataDirectory();
	void setDataDirectory(File directory);
	
	long getCircuitBuildTimeout();
	void setCircuitBuildTimeout(long time, TimeUnit unit);
	
	long getCircuitStreamTimeout();
	void setCircuitStreamTimeout(long time, TimeUnit unit);
	
	long getCircuitIdleTimeout();
	void setCircuitIdleTimeout(long time, TimeUnit unit);
	
	long getNewCircuitPeriod();
	void setNewCircuitPeriod(long time, TimeUnit unit);
	
	long getMaxCircuitDirtiness();
	void setMaxCircuitDirtiness(long time, TimeUnit unit);
	
	int getMaxClientCircuitsPending();
	void setMaxClientCircuitsPending(int value);
	
	boolean getEnforceDistinctSubnets();
	void setEnforceDistinctSubnets(boolean value);
	
	long getSocksTimeout();
	void setSocksTimeout(long value);
	
	int getNumEntryGuards();
	void setNumEntryGuards(int value);
	
	boolean getUseEntryGuards();
	void setUseEntryGuards(boolean value);
	
	List<Integer> getLongLivedPorts();
	void setLongLivedPorts(List<Integer> ports);

	List<String> getExcludeNodes();
	void setExcludeNodes(List<String> nodes);
	
	List<String> getExcludeExitNodes();
	
	void setExcludeExitNodes(List<String> nodes);
	
	List<String> getExitNodes();
	void setExitNodes(List<String> nodes);
	
	List<String> getEntryNodes();
	void setEntryNodes(List<String> nodes);
	
	boolean getStrictNodes();
	void setStrictNodes(boolean value);
	
	boolean getFascistFirewall();
	void setFascistFirewall(boolean value);
	
	List<Integer> getFirewallPorts();
	void setFirewallPorts(List<Integer> ports);
	
	boolean getSafeSocks();
	void setSafeSocks(boolean value);
	
	boolean getSafeLogging();
	void setSafeLogging(boolean value);

	boolean getWarnUnsafeSocks();
	void setWarnUnsafeSocks(boolean value);

	boolean getClientRejectInternalAddress();
	void setClientRejectInternalAddress(boolean value);
	
	boolean getHandshakeV3Enabled();
	void setHandshakeV3Enabled(boolean value);
	
	boolean getHandshakeV2Enabled();
	void setHandshakeV2Enabled(boolean value);
	
	HSDescriptorCookie getHidServAuth(String key);
	void addHidServAuth(String key, String value);
	
	AutoBoolValue getUseNTorHandshake();
	void setUseNTorHandshake(AutoBoolValue value);
	
	AutoBoolValue getUseMicrodescriptors();
	void setUseMicrodescriptors(AutoBoolValue value);

	boolean getUseBridges();
	void setUseBridges(boolean value);
	
	List<TorConfigBridgeLine> getBridges();
	void addBridge(IPv4Address address, int port);
	void addBridge(IPv4Address address, int port, HexDigest fingerprint);
	
	enum AutoBoolValue { TRUE, FALSE, AUTO }
}