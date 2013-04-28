package org.torproject.jtor.circuits.impl;

import java.util.List;
import java.util.concurrent.TimeoutException;

import org.torproject.jtor.Router;
import org.torproject.jtor.Stream;
import org.torproject.jtor.StreamConnectFailedException;
import org.torproject.jtor.circuits.path.CircuitPathChooser;

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
			removeStream(stream);
			return processStreamOpenException(e);
		}
	}

	@Override
	List<Router> choosePath(CircuitPathChooser pathChooser) {
		return pathChooser.chooseDirectoryPath();
	}
}
