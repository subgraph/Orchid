package com.subgraph.orchid.circuits;

import java.util.List;
import java.util.concurrent.TimeoutException;

import com.subgraph.orchid.Router;
import com.subgraph.orchid.Stream;
import com.subgraph.orchid.StreamConnectFailedException;
import com.subgraph.orchid.circuits.path.CircuitPathChooser;
import com.subgraph.orchid.circuits.path.PathSelectionFailedException;

public class DirectoryCircuit extends CircuitBase {

	private final Router target;
	
	protected DirectoryCircuit(CircuitManagerImpl circuitManager, Router target) {
		super(circuitManager);
		this.target = target;
	}
	protected DirectoryCircuit(CircuitManagerImpl circuitManager) {
		this(circuitManager, null);
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
	protected List<Router> choosePath(CircuitPathChooser pathChooser) throws InterruptedException, PathSelectionFailedException {
		if(target != null) {
			return pathChooser.choosePathWithFinal(target);
		} else {
			return pathChooser.chooseDirectoryPath();
		}
	}

	public CircuitType getCircuitType() {
		return CircuitType.CIRCUIT_DIRECTORY;
	}
}
