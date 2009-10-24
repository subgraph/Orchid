package org.torproject.jtor.circuits.impl;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import org.torproject.jtor.TorException;
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
	
	void build() {
		System.out.println("Building "+ circuitPath.size() +" node circuit");
		final RouterDescriptor entryRouter = circuitPath.get(0);
		
		
		createTo(entryRouter);
		
		for(int i = 1; i < circuitPath.size(); i++) 
			extendTo(circuitPath.get(i));
	
	}
	
	void createTo(RouterDescriptor targetRouter) {
		final CircuitNode newNode = new CircuitNode(targetRouter, null);
		final Cell cell = Cell.createCell(circuit.getCircuitId(), Cell.CREATE);
		cell.putByteArray(newNode.createOnionSkin());
		circuit.sendCell(cell);
		Cell c = circuit.receiveControlCell();
		processCreatedCell(newNode, c);
		circuit.appendNode(newNode);			
	}
	
	void extendTo(RouterDescriptor targetRouter) {
		System.out.println("Extending circuit to "+ targetRouter.getNickname());
		if(circuit.getCircuitLength() == 0)
			throw new TorException("Cannot EXTEND an empty circuit");
		final CircuitNode newNode = createExtendNode(targetRouter);
		final RelayCell cell = createRelayCell(newNode);
		circuit.sendRelayCellToFinalNode(cell);
		try {
			receiveExtendResponse(newNode);
			circuit.appendNode(newNode);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private CircuitNode createExtendNode(RouterDescriptor router) {
		return new CircuitNode(router, circuit.getFinalCircuitNode());
	}
	
	private RelayCell createRelayCell(CircuitNode newNode) {
		final RelayCell cell = new RelayCell(circuit.getCircuitId(), 0, RelayCell.RELAY_EXTEND);
		final RouterDescriptor router = newNode.getRouter();
		cell.putByteArray(router.getAddress().getAddressDataBytes());
		cell.putShort(router.getRouterPort());
		cell.putByteArray( newNode.createOnionSkin());
		cell.putByteArray(router.getIdentityKey().getFingerprint().getRawBytes());
		return cell;
	}
	
	private void receiveExtendResponse(CircuitNode newNode) throws IOException {
		System.out.println("Waiting for response");
		final Cell c = circuit.receiveRelayCell(RelayCell.RELAY_EXTENDED);
		c.getShort(); // recognized
		c.getShort(); // stream
		c.getInt(); // digest;
		c.getShort(); // length
		
		byte[] dhPublic = new byte[TorKeyAgreement.DH_LEN];
		c.getByteArray(dhPublic);
		byte[] keyHash = new byte[TorMessageDigest.TOR_DIGEST_SIZE];
		c.getByteArray(keyHash);
		HexDigest packetDigest = HexDigest.createFromDigestBytes(keyHash);
		BigInteger peerPublic = new BigInteger(1, dhPublic);
		newNode.setSharedSecret(peerPublic, packetDigest);
	}
	
	private void processCreatedCell(CircuitNode node, Cell cell) {
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
}
