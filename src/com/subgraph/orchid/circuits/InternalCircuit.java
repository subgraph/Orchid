package com.subgraph.orchid.circuits;

import java.util.List;
import java.util.concurrent.TimeoutException;

import com.subgraph.orchid.Router;
import com.subgraph.orchid.Stream;
import com.subgraph.orchid.StreamConnectFailedException;
import com.subgraph.orchid.circuits.path.CircuitPathChooser;
import com.subgraph.orchid.circuits.path.PathSelectionFailedException;

public class InternalCircuit extends CircuitBase {

	protected InternalCircuit(CircuitManagerImpl circuitManager) {
		super(circuitManager);
	}

	@Override
	protected List<Router> choosePath(CircuitPathChooser pathChooser)
			throws InterruptedException, PathSelectionFailedException {
		return pathChooser.chooseInternalPath();
	}

	@Override
	public void cannibalizeTo(Router target) {
		final CircuitExtender extender = new CircuitExtender(this);
		extender.extendTo(target);
	}
	
	public Stream openExitStream(String target, int port, long timeout) throws InterruptedException, TimeoutException, StreamConnectFailedException {
		final StreamImpl stream = createNewStream();
		try {
			stream.openExit(target, port, timeout);
			return stream;
		} catch (Exception e) {
			removeStream(stream);
			return processStreamOpenException(e);
		}
	}
	
	public CircuitType getCircuitType() {
		return CircuitType.CIRCUIT_INTERNAL;
	}
	
}
