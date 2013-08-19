package com.subgraph.orchid.circuits.hs;

import java.math.BigInteger;
import java.util.List;
import java.util.logging.Logger;

import com.subgraph.orchid.Cell;
import com.subgraph.orchid.RelayCell;
import com.subgraph.orchid.Router;
import com.subgraph.orchid.circuits.CircuitBase;
import com.subgraph.orchid.circuits.CircuitManagerImpl;
import com.subgraph.orchid.circuits.path.CircuitPathChooser;
import com.subgraph.orchid.circuits.path.PathSelectionFailedException;
import com.subgraph.orchid.crypto.TorKeyAgreement;
import com.subgraph.orchid.crypto.TorMessageDigest;
import com.subgraph.orchid.crypto.TorRandom;
import com.subgraph.orchid.data.HexDigest;

public class RendezvousCircuit extends CircuitBase {
	private final static Logger logger = Logger.getLogger(RendezvousCircuit.class.getName());
	
	private final static int RENDEZVOUS_COOKIE_LEN = 20;
	private final static TorRandom random = new TorRandom();
	
	private HiddenServiceCircuitNode node;
	private final byte[] cookie;
	
	protected RendezvousCircuit(CircuitManagerImpl circuitManager) {
		super(circuitManager);
		this.cookie = random.getBytes(RENDEZVOUS_COOKIE_LEN);
	}
	
	boolean establishRendezvous() {
		final RelayCell cell = createRelayCell(RelayCell.RELAY_COMMAND_ESTABLISH_RENDEZVOUS, 0, getFinalCircuitNode());
		cell.putByteArray(cookie);
		sendRelayCell(cell);
		final RelayCell response = receiveRelayCell();
		if(response == null) {
			logger.info("Timeout waiting for Rendezvous establish response");
			return false;
		} else if(response.getRelayCommand() != RelayCell.RELAY_COMMAND_RENDEZVOUS_ESTABLISHED) {
			logger.info("Response received from Rendezvous establish was not expected acknowledgement, Relay Command: "+ response.getRelayCommand());
			return false;
		} else {
			node = new HiddenServiceCircuitNode(getFinalCircuitNode());
			return true;
		}
	}
	
	boolean processRendezvous2() {
		if(node == null) {
			throw new IllegalStateException("Can only be called after successful rendezvous establishment");
		}
		final RelayCell cell = receiveRelayCell();
		if(cell == null) {
			logger.info("Timeout waiting for RENDEZVOUS2");
			return false;
		} else if (cell.getRelayCommand() != RelayCell.RELAY_COMMAND_RENDEZVOUS2) {
			logger.info("Unexpected Relay cell type received while waiting for RENDEZVOUS2: "+ cell.getRelayCommand());
			return false;
		}
		final BigInteger peerPublic = readPeerPublic(cell);
		final HexDigest handshakeDigest = readHandshakeDigest(cell);
		if(peerPublic == null || handshakeDigest == null) {
			return false;
		}
		node.setSharedSecret(peerPublic, handshakeDigest);
		appendNode(node);
		return true;
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
		return HexDigest.createDigestForData(digestBytes);
	}
	
	
	byte[] getCookie() {
		return cookie;
	}

	Router getRendezvousRouter() {
		return getFinalRouter();
	}

	@Override
	protected List<Router> choosePath(CircuitPathChooser pathChooser)
			throws InterruptedException, PathSelectionFailedException {
		return pathChooser.chooseInternalPath();
	}

}
