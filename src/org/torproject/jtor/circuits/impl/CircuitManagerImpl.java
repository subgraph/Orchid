package org.torproject.jtor.circuits.impl;

import java.util.List;

import org.torproject.jtor.circuits.Circuit;
import org.torproject.jtor.circuits.CircuitManager;
import org.torproject.jtor.circuits.Stream;
import org.torproject.jtor.directory.Directory;
import org.torproject.jtor.directory.Router;

public class CircuitManagerImpl implements CircuitManager {

	private final Directory directory;
	private final ConnectionManagerImpl connectionManager;
	
	public CircuitManagerImpl(Directory directory, ConnectionManagerImpl connectionManager) {
		this.directory = directory;
		this.connectionManager = connectionManager;
	}
	
	public Circuit createCircuitFromNicknames(List<String> nicknamePath) {
		final List<Router> path = directory.getRouterListByNames(nicknamePath);
		return createCircuitFromPath(path);
	}

	public Circuit createCircuitFromPath(List<Router> path) {
		return CircuitImpl.create(connectionManager, path);
	}

	public Stream getDirectoryStream() {
		
		// TODO Auto-generated method stub
		return null;
	}

}
