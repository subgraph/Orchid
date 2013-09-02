package com.subgraph.orchid.circuits.hs;

import java.math.BigInteger;
import java.util.logging.Logger;

import com.subgraph.orchid.Cell;
import com.subgraph.orchid.CircuitNode;
import com.subgraph.orchid.HiddenServiceCircuit;
import com.subgraph.orchid.InternalCircuit;
import com.subgraph.orchid.RelayCell;
import com.subgraph.orchid.Router;
import com.subgraph.orchid.circuits.CircuitNodeImpl;
import com.subgraph.orchid.crypto.TorKeyAgreement;
import com.subgraph.orchid.crypto.TorMessageDigest;
import com.subgraph.orchid.crypto.TorRandom;
import com.subgraph.orchid.data.HexDigest;

public class RendezvousProcessor {
	private final static Logger logger = Logger.getLogger(RendezvousProcessor.class.getName());
	
	private final static int RENDEZVOUS_COOKIE_LEN = 20;
	private final static TorRandom random = new TorRandom();
	
	private CircuitNode node;
	private final InternalCircuit circuit;
	private final byte[] cookie;
	
	protected RendezvousProcessor(InternalCircuit circuit) {
		this.circuit = circuit;
		this.cookie = random.getBytes(RENDEZVOUS_COOKIE_LEN);
	}
	
	boolean establishRendezvous() {
		final RelayCell cell = circuit.createRelayCell(RelayCell.RELAY_COMMAND_ESTABLISH_RENDEZVOUS, 0, circuit.getFinalCircuitNode());
		cell.putByteArray(cookie);
		circuit.sendRelayCell(cell);
		final RelayCell response = circuit.receiveRelayCell();
		if(response == null) {
			logger.info("Timeout waiting for Rendezvous establish response");
			return false;
		} else if(response.getRelayCommand() != RelayCell.RELAY_COMMAND_RENDEZVOUS_ESTABLISHED) {
			logger.info("Response received from Rendezvous establish was not expected acknowledgement, Relay Command: "+ response.getRelayCommand());
			return false;
		} else {
			node = CircuitNodeImpl.createAnonymous(circuit.getFinalCircuitNode());
			return true;
		}
	}
	
	HiddenServiceCircuit processRendezvous2() {
		if(node == null) {
			throw new IllegalStateException("Can only be called after successful rendezvous establishment");
		}
		final RelayCell cell = circuit.receiveRelayCell();
		if(cell == null) {
			logger.info("Timeout waiting for RENDEZVOUS2");
			return null;
		} else if (cell.getRelayCommand() != RelayCell.RELAY_COMMAND_RENDEZVOUS2) {
			logger.info("Unexpected Relay cell type received while waiting for RENDEZVOUS2: "+ cell.getRelayCommand());
			return null;
		}
		final BigInteger peerPublic = readPeerPublic(cell);
		final HexDigest handshakeDigest = readHandshakeDigest(cell);
		if(peerPublic == null || handshakeDigest == null) {
			return null;
		}
		node.setSharedSecret(peerPublic, handshakeDigest);
		return circuit.connectHiddenService(node);
	}
	
	private BigInteger readPeerPublic(Cell cell) {
		final byte[] dhPublic = new byte[TorKeyAgreement.DH_LEN];
		cell.getByteArray(dhPublic);
		final BigInteger peerPublic = new BigInteger(1, dhPublic);
		if(!TorKeyAgreement.isValidPublicValue(peerPublic)) {
			logger.warning("Illegal DH public value received: "+ peerPublic);
			return null;
		}
		return peerPublic;
	}
	
	byte[] getPublicKeyBytes() {
		if(node == null) {
			throw new IllegalStateException("Can only be called after successful rendezvous establish");
		}
		return node.getPublicKeyBytes();
	}

	HexDigest readHandshakeDigest(Cell cell) {
		final byte[] digestBytes = new byte[TorMessageDigest.TOR_DIGEST_SIZE];
		cell.getByteArray(digestBytes);
		return HexDigest.createFromDigestBytes(digestBytes);
	}
	
	
	byte[] getCookie() {
		return cookie;
	}

	Router getRendezvousRouter() {
		return circuit.getFinalCircuitNode().getRouter();
	}
}
