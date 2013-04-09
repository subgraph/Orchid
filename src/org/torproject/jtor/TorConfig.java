package org.torproject.jtor;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.concurrent.TimeUnit;


public interface TorConfig {
	
	@ConfigVar(type=ConfigVarType.PATH, defaultValue="~/.jtor")
	File getDataDirectory();
	void setDataDirectory(File directory);
	
	@ConfigVar(type=ConfigVarType.INTERVAL, defaultValue="60 seconds")
	long getCircuitBuildTimeout();
	void setCircuitBuildTimeout(long time, TimeUnit unit);
	
	
	@ConfigVar(type=ConfigVarType.INTERVAL, defaultValue="1 hour")
	long getCircuitIdleTimeout();
	void setCircuitIdleTimeout(long time, TimeUnit unit);
	
	@ConfigVar(type=ConfigVarType.INTERVAL, defaultValue="30 seconds")
	long getNewCircuitPeriod();
	void setNewCircuitPeriod(long time, TimeUnit unit);
	
	@ConfigVar(type=ConfigVarType.INTERVAL, defaultValue="10 minutes")
	long getMaxCircuitDirtiness();
	void setMaxCircuitDirtiness(long time, TimeUnit unit);
	
	
	@ConfigVar(type=ConfigVarType.INTEGER, defaultValue="32")
	int getMaxClientCircuitsPending();
	void setMaxClientCircuitsPending(int value);
	
	@ConfigVar(type=ConfigVarType.BOOLEAN, defaultValue="true")
	boolean getEnforceDistinctSubnets();
	void setEnforceDistinctSubnets(boolean value);
	
	@ConfigVar(type=ConfigVarType.INTERVAL, defaultValue="2 minutes")
	long getSocksTimeout();
	void setSocksTimeout(long value);
	
	@ConfigVar(type=ConfigVarType.INTEGER, defaultValue="3")
	int getNumEntryGuards();
	void setNumEntryGuards(int value);
	
	@ConfigVar(type=ConfigVarType.BOOLEAN, defaultValue="true")
	boolean getUseEntryGuards();
	void setUseEntryGuards(boolean value);
	
	@ConfigVar(type=ConfigVarType.PORTLIST, defaultValue="21,22,706,1863,5050,5190,5222,5223,6523,6667,6697,8300")
	List<Integer> getLongLivedPorts();
	void setLongLivedPorts(List<Integer> ports);

	@ConfigVar(type=ConfigVarType.STRINGLIST)
	List<String> getExcludeNodes();
	void setExcludeNodes(List<String> nodes);
	
	@ConfigVar(type=ConfigVarType.STRINGLIST)
	List<String> getExcludeExitNodes();
	
	void setExcludeExitNodes(List<String> nodes);
	
	@ConfigVar(type=ConfigVarType.STRINGLIST)
	List<String> getExitNodes();
	void setExitNodes(List<String> nodes);
	
	@ConfigVar(type=ConfigVarType.STRINGLIST)
	List<String> getEntryNodes();
	void setEntryNodes(List<String> nodes);
	
	@ConfigVar(type=ConfigVarType.BOOLEAN, defaultValue="false")
	boolean getStrictNodes();
	void setStrictNodes(boolean value);
	
	
	@ConfigVar(type=ConfigVarType.BOOLEAN, defaultValue="false")
	boolean getFascistFirewall();
	void setFascistFirewall(boolean value);
	
	@ConfigVar(type=ConfigVarType.PORTLIST, defaultValue="80,443")
	List<Integer> getFirewallPorts();
	void setFirewallPorts(List<Integer> ports);
	
	enum ConfigVarType { INTEGER, STRING, BOOLEAN, INTERVAL, PORTLIST, STRINGLIST, PATH };
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@interface ConfigVar {
		ConfigVarType type();
		String defaultValue() default "";
	}
}