package com.subgraph.orchid.circuits.hs;

import java.util.concurrent.Callable;
import java.util.logging.Logger;

import com.subgraph.orchid.Circuit;
import com.subgraph.orchid.Directory;
import com.subgraph.orchid.Router;
import com.subgraph.orchid.TorException;
import com.subgraph.orchid.circuits.CircuitManagerImpl;

public class RendezvousCircuitBuilder implements Callable<Circuit>{
	private final Logger logger = Logger.getLogger(RendezvousCircuitBuilder.class.getName());
	
	private final Directory directory;
	
	private final CircuitManagerImpl circuitManager;
	private final HiddenService hiddenService;
	private final HSDescriptor serviceDescriptor;
	
	public RendezvousCircuitBuilder(Directory directory, CircuitManagerImpl circuitManager, HiddenService hiddenService, HSDescriptor descriptor) {
		this.directory = directory;
		this.circuitManager = circuitManager;
		this.hiddenService = hiddenService;
		this.serviceDescriptor = descriptor;
	}
	
	public Circuit call() throws Exception {
		
		logger.fine("Opening rendezvous circuit for "+ logServiceName());
		
		final Circuit rendezvous = circuitManager.getCleanInternalCircuit();
		logger.fine("Establishing rendezvous for "+ logServiceName());
		RendezvousProcessor rp = new RendezvousProcessor(rendezvous);
		if(!rp.establishRendezvous()) {
			rendezvous.markForClose();
			return null;
		}
		logger.fine("Opening introduction circuit for "+ logServiceName());
		final IntroductionProcessor introductionProcessor = openIntroduction();
		if(introductionProcessor == null) {
			logger.info("Failed to open connection to any introduction point");
			rendezvous.markForClose();
			return null;
		}
		logger.fine("Sending introduce cell for "+ logServiceName());
		
		final boolean icResult = introductionProcessor.sendIntroduce(introductionProcessor.getServiceKey(), rp.getPublicKeyBytes(), rp.getCookie(), rp.getRendezvousRouter());
		introductionProcessor.markCircuitForClose();
		if(!icResult) {
			rendezvous.markForClose();
			return null;
		}
		logger.fine("Processing RV2 for "+ logServiceName());
		if(!rp.processRendezvous2()) {
			rendezvous.markForClose();
			return null;
		}

		logger.fine("Rendezvous circuit opened for "+ logServiceName());
		
		return rendezvous;
	}
	
	private String logServiceName() {
		return hiddenService.getOnionAddressForLogging();
	}
	
	private IntroductionProcessor openIntroduction() {
		for(IntroductionPoint ip: serviceDescriptor.getShuffledIntroductionPoints()) {
			final Circuit circuit = attemptOpenIntroductionCircuit(ip);
			if(circuit != null) {
				return new IntroductionProcessor(circuit, ip);
			}
		}
		return null;
	}
	
	private Circuit attemptOpenIntroductionCircuit(IntroductionPoint ip) {
		final Router r = directory.getRouterByIdentity(ip.getIdentity());
		if(r == null) {
			return null;
		}
		
		try {
			final Circuit circuit = circuitManager.getCleanInternalCircuit();
			circuit.cannibalizeTo(r);
			return circuit;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return null;
		} catch (TorException e) {
			logger.fine("cannibalizeTo() failed : "+ e.getMessage());
			return null;
		}
	}
}
