package org.torproject.jtor.circuits.impl;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import org.torproject.jtor.TorException;
import org.torproject.jtor.circuits.CircuitBuildHandler;
import org.torproject.jtor.circuits.CircuitNode;
import org.torproject.jtor.circuits.cells.Cell;
import org.torproject.jtor.circuits.cells.RelayCell;
import org.torproject.jtor.crypto.TorKeyAgreement;
import org.torproject.jtor.crypto.TorMessageDigest;
import org.torproject.jtor.data.HexDigest;
import org.torproject.jtor.directory.RouterDescriptor;

/*
 * Utility class used by CircuitImpl that manages setting up a circuit 
 * through a specified path of router nodes. 
 */
class CircuitBuilder {
	
	private final List<RouterDescriptor> circuitPath;
	private final CircuitImpl circuit;
	
	CircuitBuilder(CircuitImpl circuit, List<RouterDescriptor> path) {
		this.circuit = circuit;
		this.circuitPath = path;	
	}
	
	boolean build(CircuitBuildHandler handler) {
		System.out.println("Building "+ circuitPath.size() +" node circuit");
		try {
			runCircuitBuild(handler);
		} catch(TorException e) {
			if(handler != null) 
				handler.circuitBuildFailed(e.getMessage());
			return false;
		}
		if(handler != null)
			handler.circuitBuildCompleted(circuit);
		return true;
	}
	
	private void runCircuitBuild(CircuitBuildHandler handler) {
		final RouterDescriptor entryRouter = circuitPath.get(0);
		final CircuitNode firstNode = createTo(entryRouter);
		if(handler != null)
			handler.nodeAdded(firstNode);
		
		for(int i = 1; i < circuitPath.size(); i++) {
			final CircuitNode extendedNode = extendTo(circuitPath.get(i));
			if(handler != null)
				handler.nodeAdded(extendedNode);
		}
	}
	
	CircuitNode createTo(RouterDescriptor targetRouter) {
		final CircuitNodeImpl newNode = new CircuitNodeImpl(targetRouter, null);
		sendCreateCell(newNode);
		receiveAndProcessCreateResponse(newNode);
		return newNode;
	}
	
	private void sendCreateCell(CircuitNodeImpl node) {
		final Cell cell = CellImpl.createCell(circuit.getCircuitId(), Cell.CREATE);
		cell.putByteArray(node.createOnionSkin());
		circuit.sendCell(cell);
	}
	
	private void receiveAndProcessCreateResponse(CircuitNodeImpl node) {
		final Cell cell = circuit.receiveControlCellResponse();
		processCreatedCell(node, cell);
		circuit.appendNode(node);
	}
	
	private void processCreatedCell(CircuitNodeImpl node, Cell cell) {
		final byte[] cellBytes = cell.getCellBytes();
		final byte[] dhData = new byte[TorKeyAgreement.DH_LEN];
		final byte[] hash = new byte[TorMessageDigest.TOR_DIGEST_SIZE];
		
		int offset = Cell.CELL_HEADER_LEN;
		System.arraycopy(cellBytes, offset, dhData, 0, TorKeyAgreement.DH_LEN);
		
		offset += TorKeyAgreement.DH_LEN;
		System.arraycopy(cellBytes, offset, hash, 0, TorMessageDigest.TOR_DIGEST_SIZE);
		
		BigInteger peerPublic = new BigInteger(1, dhData);
		node.setSharedSecret(peerPublic, HexDigest.createFromDigestBytes(hash));
	}
	
	CircuitNode extendTo(RouterDescriptor targetRouter) {
		System.out.println("Extending circuit to "+ targetRouter.getNickname());
		if(circuit.getCircuitLength() == 0)
			throw new TorException("Cannot EXTEND an empty circuit");
		final CircuitNodeImpl newNode = createExtendNode(targetRouter);
		final RelayCell cell = createRelayExtendCell(newNode);
		circuit.sendRelayCellToFinalNode(cell);
		try {
			receiveExtendResponse(newNode);
			circuit.appendNode(newNode);
			return newNode;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	private CircuitNodeImpl createExtendNode(RouterDescriptor router) {
		return new CircuitNodeImpl(router, circuit.getFinalCircuitNode());
	}
	
	private RelayCell createRelayExtendCell(CircuitNodeImpl newNode) {
		final RelayCell cell = new RelayCellImpl(circuit.getFinalCircuitNode(), circuit.getCircuitId(), 0, RelayCell.RELAY_EXTEND);
		final RouterDescriptor router = newNode.getRouter();
		cell.putByteArray(router.getAddress().getAddressDataBytes());
		cell.putShort(router.getRouterPort());
		cell.putByteArray( newNode.createOnionSkin());
		cell.putByteArray(router.getIdentityKey().getFingerprint().getRawBytes());
		return cell;
	}
	
	private void receiveExtendResponse(CircuitNodeImpl newNode) throws IOException {
		System.out.println("Waiting for response");
		final RelayCell cell = circuit.receiveRelayResponse(RelayCell.RELAY_EXTENDED);
		
		byte[] dhPublic = new byte[TorKeyAgreement.DH_LEN];
		cell.getByteArray(dhPublic);
		byte[] keyHash = new byte[TorMessageDigest.TOR_DIGEST_SIZE];
		cell.getByteArray(keyHash);
		HexDigest packetDigest = HexDigest.createFromDigestBytes(keyHash);
		BigInteger peerPublic = new BigInteger(1, dhPublic);
		newNode.setSharedSecret(peerPublic, packetDigest);
	}
}
