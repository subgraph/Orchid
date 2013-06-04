package com.subgraph.orchid.circuits;

import java.util.List;
import java.util.concurrent.TimeoutException;

import com.subgraph.orchid.Router;
import com.subgraph.orchid.Stream;
import com.subgraph.orchid.StreamConnectFailedException;
import com.subgraph.orchid.circuits.path.CircuitPathChooser;

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
	protected List<Router> choosePath(CircuitPathChooser pathChooser) {
		return pathChooser.chooseDirectoryPath();
	}
}
