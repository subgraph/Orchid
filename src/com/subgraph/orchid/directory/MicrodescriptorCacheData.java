package com.subgraph.orchid.directory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.subgraph.orchid.RouterMicrodescriptor;
import com.subgraph.orchid.data.HexDigest;
import com.subgraph.orchid.misc.GuardedBy;



public class MicrodescriptorCacheData {

	/** 7 days */
	private final static long EXPIRY_PERIOD = 7 * 24 * 60 * 60 * 1000;
	
	@GuardedBy("this")
	private final Map<HexDigest, RouterMicrodescriptor> microdescriptorMap;
	
	@GuardedBy("this")
	private final List<RouterMicrodescriptor> allDescriptors;
	
	public MicrodescriptorCacheData() {
		this.microdescriptorMap = new HashMap<HexDigest, RouterMicrodescriptor>();
		this.allDescriptors = new ArrayList<RouterMicrodescriptor>();
	}
	
	synchronized RouterMicrodescriptor findByDigest(HexDigest digest) {
		return microdescriptorMap.get(digest);
	}
	
	synchronized List<RouterMicrodescriptor> getAllDescriptors() {
		return new ArrayList<RouterMicrodescriptor>(allDescriptors);
	}

	synchronized boolean addDescriptor(RouterMicrodescriptor md) {
		if(microdescriptorMap.containsKey(md.getDescriptorDigest())) {
			return false;
		}
		microdescriptorMap.put(md.getDescriptorDigest(), md);
		allDescriptors.add(md);
		return true;
	}
	
	synchronized void clear() {
		microdescriptorMap.clear();
		allDescriptors.clear();
	}
	
	synchronized int cleanExpired() {
		final Set<RouterMicrodescriptor> expired = getExpiredSet();

		if(expired.isEmpty()) {
			return 0;
		}
		
		clear();
		int dropped = 0;
		for(RouterMicrodescriptor md: allDescriptors) {
			if(expired.contains(md)) {
				dropped += md.getBodyLength();
			} else {
				addDescriptor(md);
			}
		}
		
		return dropped;
	}

	private Set<RouterMicrodescriptor> getExpiredSet() {
		final long now = System.currentTimeMillis();
		final Set<RouterMicrodescriptor> expired = new HashSet<RouterMicrodescriptor>();
		for(RouterMicrodescriptor md: allDescriptors) {
			if(isExpired(md, now)) {
				expired.add(md);
			}
		}
		return expired;
	}

	private boolean isExpired(RouterMicrodescriptor md, long now) {
		return md.getLastListed() != 0 && md.getLastListed() < (now - EXPIRY_PERIOD);
	}
}
