package com.subgraph.orchid.circuits;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import com.subgraph.orchid.Router;
import com.subgraph.orchid.Stream;
import com.subgraph.orchid.StreamConnectFailedException;
import com.subgraph.orchid.circuits.path.CircuitPathChooser;
import com.subgraph.orchid.circuits.path.PathSelectionFailedException;
import com.subgraph.orchid.data.IPv4Address;
import com.subgraph.orchid.data.exitpolicy.ExitTarget;

public class ExitCircuit extends CircuitBase {
	
	private final Router exitRouter;
	private final Set<ExitTarget> failedExitRequests;

	ExitCircuit(CircuitManagerImpl circuitManager, Router exitRouter) {
		super(circuitManager);
		this.exitRouter = exitRouter;
		this.failedExitRequests = new HashSet<ExitTarget>();
	}
	
	Router getExitRouter() {
		return exitRouter;
	}
	
	public Stream openExitStream(IPv4Address address, int port, long timeout) throws InterruptedException, TimeoutException, StreamConnectFailedException {
		return openExitStream(address.toString(), port, timeout);
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
	
	public void recordFailedExitTarget(ExitTarget target) {
		synchronized(failedExitRequests) {
			failedExitRequests.add(target);
		}
	}

	public boolean canHandleExitTo(ExitTarget target) {
		synchronized(failedExitRequests) {
			if(failedExitRequests.contains(target)) {
				return false;
			}
		}
		
		if(isMarkedForClose()) {
			return false;
		}

		if(target.isAddressTarget()) {
			return exitRouter.exitPolicyAccepts(target.getAddress(), target.getPort());
		} else {
			return exitRouter.exitPolicyAccepts(target.getPort());
		}
	}
	
	public boolean canHandleExitToPort(int port) {
		return exitRouter.exitPolicyAccepts(port);
	}

	@Override
	protected List<Router> choosePath(CircuitPathChooser pathChooser) throws InterruptedException, PathSelectionFailedException {
		return pathChooser.choosePathWithExit(exitRouter);
	}

	public CircuitType getCircuitType() {
		return CircuitType.CIRCUIT_EXIT;
	}
}
