package com.subgraph.orchid.circuits.hs;

import java.util.concurrent.Callable;
import java.util.logging.Logger;

import com.subgraph.orchid.Circuit;
import com.subgraph.orchid.CircuitBuildHandler;
import com.subgraph.orchid.CircuitNode;
import com.subgraph.orchid.Connection;
import com.subgraph.orchid.ConnectionCache;
import com.subgraph.orchid.Directory;
import com.subgraph.orchid.Router;
import com.subgraph.orchid.circuits.CircuitBase;
import com.subgraph.orchid.circuits.CircuitBuildTask;
import com.subgraph.orchid.circuits.CircuitCreationRequest;
import com.subgraph.orchid.circuits.CircuitManagerImpl;
import com.subgraph.orchid.circuits.path.CircuitPathChooser;

public class RendezvousCircuitBuilder implements Callable<RendezvousCircuit>{
	private final Logger logger = Logger.getLogger(RendezvousCircuitBuilder.class.getName());
	
	private final static int MAX_CIRCUIT_OPEN_ATTEMPTS = 5;
	
	private final Directory directory;
	private final ConnectionCache connectionCache;
	private final CircuitManagerImpl circuitManager;
	private final CircuitPathChooser pathChooser;
	private final HSDescriptor serviceDescriptor;
	
	public RendezvousCircuitBuilder(Directory directory, ConnectionCache connectionCache, CircuitManagerImpl circuitManager, CircuitPathChooser pathChooser, HSDescriptor descriptor) {
		this.directory = directory;
		this.connectionCache = connectionCache;
		this.circuitManager = circuitManager;
		this.pathChooser = pathChooser;
		this.serviceDescriptor = descriptor;
	}
	
	public RendezvousCircuit call() throws Exception {
		logger.info("Opening rendezvous circuit");
		final RendezvousCircuit circuit = openRendezvous();
		logger.info("Establishing rendezvous");
		if(!circuit.establishRendezvous()) {
			circuit.markForClose();
			return null;
		}
		logger.info("Opening introduction circuit");
		final IntroductionCircuit introductionCircuit = openIntroduction();
		if(introductionCircuit == null) {
			logger.info("Failed to open connection to any introduction point");
			circuit.markForClose();
			return null;
		}
		logger.info("Sending introduce cell");
		final boolean icResult = introductionCircuit.sendIntroduce(serviceDescriptor.getPermanentKey(), circuit.getPublicKeyBytes(), circuit.getCookie(), circuit.getRendezvousRouter());
		introductionCircuit.markForClose();
		if(!icResult) {
			circuit.markForClose();
			return null;
		}
		logger.info("Processing RV2");
		if(!circuit.processRendezvous2()) {
			circuit.markForClose();
			return null;
		}

		logger.info("finished");
		
		return circuit;
	}
	
	private IntroductionCircuit openIntroduction() {
		for(IntroductionPoint ip: serviceDescriptor.getShuffledIntroductionPoints()) {
			final IntroductionCircuit circuit = attemptOpenIntroductionCircuit(ip);
			if(circuit != null) {
				return circuit;
			}
		}
		return null;
	}
	
	private IntroductionCircuit attemptOpenIntroductionCircuit(IntroductionPoint ip) {
		final Router r = directory.getRouterByIdentity(ip.getIdentity());
		if(r == null) {
			return null;
		}
		for(int i = 0; i < MAX_CIRCUIT_OPEN_ATTEMPTS; i++) {
			final IntroductionCircuit circuit = new IntroductionCircuit(circuitManager, r, ip);
			if(attemptOpenCircuit(circuit)) {
				return circuit;
			}
		}
		return null;
	}

	private RendezvousCircuit openRendezvous() {
		for(int i = 0; i < MAX_CIRCUIT_OPEN_ATTEMPTS; i++) {
			final RendezvousCircuit circuit = new RendezvousCircuit(circuitManager);
			if(attemptOpenCircuit(circuit)) {
				return circuit;
			}
		}
		return null;
	}

	private boolean attemptOpenCircuit(CircuitBase circuit) {
		final CircuitBuildResult result = new CircuitBuildResult();
		final CircuitCreationRequest request = new CircuitCreationRequest(pathChooser, circuit, result);
		final CircuitBuildTask task = new CircuitBuildTask(request, connectionCache);
		task.run();
		return result.isSuccessful();
	}

	private static class CircuitBuildResult implements CircuitBuildHandler {
		private boolean isFailed;
		
		public void connectionCompleted(Connection connection) {}
		public void nodeAdded(CircuitNode node) {
			System.out.println("node added: "+ node);
		}
		public void circuitBuildCompleted(Circuit circuit) {}
		
		public void connectionFailed(String reason) {
			isFailed = true;
		}

		public void circuitBuildFailed(String reason) {
			isFailed = true;
		}
		
		boolean isSuccessful() {
			return !isFailed;
		}
		
	}
}
