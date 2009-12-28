package org.torproject.jtor.circuits.impl;

import java.util.ArrayList;
import java.util.List;

import org.torproject.jtor.directory.Router;

public class NodeChoiceConstraints {
	
	private boolean needUptime;
	private boolean needCapacity;
	private boolean needGuard;
	private boolean weightAsExit;
	private boolean weightAsGuard;
	private List<Router> excludedRouters = new ArrayList<Router>();
	private List<String> excludedFamilies = new ArrayList<String>();
	
	boolean getNeedUptime() {
		return needUptime;
	}
	
	boolean getNeedCapacity() {
		return needCapacity;
	}
	
	boolean getWeightAsExit() {
		return weightAsExit;
	}
	
	boolean getWeightAsGuard() {
		return weightAsGuard;
	}
	
	boolean getNeedGuard() {
		return needGuard;
	}
	
	List<Router> getExcludedRouters() {
		return excludedRouters;
	}
	
	List<String> getExcludedFamilies() {
		return excludedFamilies;
	}
	void setNeedUptime(boolean flag) {
		needUptime = flag;
	}
	
	void setNeedCapacity(boolean flag) {
		needCapacity = flag;
	}
	
	void setNeedGuard(boolean flag) {
		needGuard = flag;
	}
	
	void addExcludedRouter(Router router) {
		excludedRouters.add(router);
	}
	void setWeightAsExit(boolean flag) {
		if(flag)
			weightAsGuard = false;
		weightAsExit = true;
	}
	
	void setWeightAsGuard(boolean flag) {
		if(flag)
			weightAsExit = false;
		weightAsGuard = false;
	}
	

}
