package com.subgraph.orchid.circuits.hs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.subgraph.orchid.Circuit;
import com.subgraph.orchid.ConnectionCache;
import com.subgraph.orchid.Directory;
import com.subgraph.orchid.Stream;
import com.subgraph.orchid.circuits.CircuitManagerImpl;
import com.subgraph.orchid.circuits.path.CircuitPathChooser;

public class HiddenServiceManager {
	private final static Logger logger = Logger.getLogger(HiddenServiceManager.class.getName());
	
	private final Map<String, HiddenService> hiddenServices;
	private final HSDirectories hsDirectories;
	private final ConnectionCache connectionCache;
	private final CircuitManagerImpl circuitManager;
	private final CircuitPathChooser pathChooser;
	
	public HiddenServiceManager(Directory directory, ConnectionCache connectionCache, CircuitManagerImpl circuitManager, CircuitPathChooser pathChooser) {
		this.hiddenServices = new HashMap<String, HiddenService>();
		this.hsDirectories = new HSDirectories(directory);
		this.connectionCache = connectionCache;
		this.circuitManager = circuitManager;
		this.pathChooser = pathChooser;
	}
	
	public Stream getStreamTo(String onion, int port) {
		logger.warning("Stream requested for: "+ onion + ":"+ port);
		getDescriptorFor(onion);
		return null;
	}
	
	Circuit getCircuitTo(String onion) {
		return null;
	}

	HSDescriptor getDescriptorFor(String onion) {
		final HiddenService hs = getHiddenServiceForOnion(onion);
		if(hs.hasCurrentDescriptor()) {
			return hs.getDescriptor();
		}
		final HSDescriptor descriptor = downloadDescriptorFor(hs);
		hs.setDescriptor(descriptor);
		return descriptor;
	}
	
	private HSDescriptor downloadDescriptorFor(HiddenService hs) {
		logger.warning("Downloading descriptor for "+ hs);
		final List<HSDescriptorDirectory> dirs = hsDirectories.getDirectoriesForHiddenService(hs);
		final HSDescriptorDownloader downloader = new HSDescriptorDownloader(connectionCache, circuitManager, pathChooser, hs, dirs);
		return downloader.downloadDescriptor();
	}

	HiddenService getHiddenServiceForOnion(String onion) {
		final String key = onion.endsWith(".onion") ? onion.substring(0, onion.length() - 6) : onion;
		if(!hiddenServices.containsKey(key)) {
			hiddenServices.put(key, new HiddenService(key));
		}
		return hiddenServices.get(key);
	}	
}
