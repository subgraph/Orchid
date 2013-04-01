package org.torproject.jtor.circuits.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.torproject.jtor.circuits.OpenStreamResponse;
import org.torproject.jtor.circuits.path.CircuitPathChooser;
import org.torproject.jtor.circuits.path.PathSelectionFailedException;
import org.torproject.jtor.data.IPv4Address;
import org.torproject.jtor.data.exitpolicy.ExitTarget;
import org.torproject.jtor.directory.Router;

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
	
	public OpenStreamResponse openExitStream(IPv4Address address, int port) {
		return openExitStream(address.toString(), port);
	}

	public OpenStreamResponse openExitStream(String target, int port) {
		final StreamImpl stream = createNewStream();
		final OpenStreamResponse response = stream.openExit(target, port);
		processOpenStreamResponse(stream, response);
		return response;
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
	List<Router> choosePath(CircuitPathChooser pathChooser) throws InterruptedException, PathSelectionFailedException {
		return pathChooser.choosePathWithExit(exitRouter);
	}
}
