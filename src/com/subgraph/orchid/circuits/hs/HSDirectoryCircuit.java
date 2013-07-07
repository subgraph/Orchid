package com.subgraph.orchid.circuits.hs;

import java.util.List;
import java.util.concurrent.TimeoutException;

import com.subgraph.orchid.Router;
import com.subgraph.orchid.Stream;
import com.subgraph.orchid.StreamConnectFailedException;
import com.subgraph.orchid.circuits.CircuitBase;
import com.subgraph.orchid.circuits.CircuitManagerImpl;
import com.subgraph.orchid.circuits.StreamImpl;
import com.subgraph.orchid.circuits.path.CircuitPathChooser;
import com.subgraph.orchid.circuits.path.PathSelectionFailedException;

public class HSDirectoryCircuit extends CircuitBase {

	private final Router hsDirectory;
	
	protected HSDirectoryCircuit(CircuitManagerImpl circuitManager, Router hsDirectory) {
		super(circuitManager);
		this.hsDirectory = hsDirectory;
	}

	public Stream openDirectoryStream(long timeout) throws InterruptedException, TimeoutException, StreamConnectFailedException {
		final StreamImpl stream = createNewStream();
		try {
			stream.openDirectory(timeout);
			return stream;
		} catch (StreamConnectFailedException e) {
			removeStream(stream);
			return processStreamOpenException(e);
		}
		
	}
	@Override
	protected List<Router> choosePath(CircuitPathChooser pathChooser)
			throws InterruptedException, PathSelectionFailedException {
		// XXX should rename choosePathWithExit()
		return pathChooser.choosePathWithExit(hsDirectory);
	}
}
