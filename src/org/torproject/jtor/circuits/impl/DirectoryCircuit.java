package org.torproject.jtor.circuits.impl;

import java.util.List;
import java.util.concurrent.TimeoutException;

import org.torproject.jtor.circuits.Stream;
import org.torproject.jtor.circuits.StreamConnectFailedException;
import org.torproject.jtor.circuits.path.CircuitPathChooser;
import org.torproject.jtor.directory.Router;

public class DirectoryCircuit extends CircuitBase {

	protected DirectoryCircuit(CircuitManagerImpl circuitManager) {
		super(circuitManager);
	}
	
	boolean isDirectoryCircuit() {
		return true;
	}

	public Stream openDirectoryStream(long timeout) throws InterruptedException, TimeoutException, StreamConnectFailedException {
		final StreamImpl stream = createNewStream();
		try {
			stream.openDirectory(timeout);
			return stream;
		} catch (Exception e) {
			return processStreamOpenException(e);
		}
	}

	@Override
	List<Router> choosePath(CircuitPathChooser pathChooser) {
		return pathChooser.chooseDirectoryPath();
	}
}
