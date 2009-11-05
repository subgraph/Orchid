package org.torproject.jtor.circuits.impl;

import java.math.BigInteger;

import org.torproject.jtor.TorException;
import org.torproject.jtor.circuits.CircuitNode;
import org.torproject.jtor.circuits.cells.Cell;
import org.torproject.jtor.circuits.cells.RelayCell;
import org.torproject.jtor.crypto.HybridEncryption;
import org.torproject.jtor.crypto.TorKeyAgreement;
import org.torproject.jtor.crypto.TorMessageDigest;
import org.torproject.jtor.data.HexDigest;
import org.torproject.jtor.directory.RouterDescriptor;

class CircuitNodeImpl implements CircuitNode {
	static CircuitNodeImpl createForRouter(RouterDescriptor router) {
		return new CircuitNodeImpl(router);
	}
	
	private final TorKeyAgreement dhContext;
	private final RouterDescriptor routerDescriptor;
	private CircuitNodeCryptoState cryptoState;
	private final CircuitNodeImpl previousNode;
	
	private CircuitNodeImpl(RouterDescriptor router) {
		this(router, null);
	}
	
	CircuitNodeImpl(RouterDescriptor router, CircuitNodeImpl previous) {
		previousNode = previous;
		this.routerDescriptor = router;
		this.dhContext = new TorKeyAgreement();
	}
	
	public RouterDescriptor getRouter() {
		return routerDescriptor;
	}
	
	void setSharedSecret(BigInteger peerPublic, HexDigest packetDigest) {
		if(!TorKeyAgreement.isValidPublicValue(peerPublic))
			throw new TorException("Illegal DH public value");
		
		final byte[] sharedSecret = dhContext.getSharedSecret(peerPublic);
		deriveKeys(sharedSecret);
		if(!cryptoState.verifyPacketDigest(packetDigest))
			throw new TorException("Digest verification failed!");
	}
	
	public CircuitNodeImpl getPreviousNode() {
		return previousNode;
	}
	
	public void encryptForwardCell(RelayCell cell) {
		cryptoState.encryptForwardCell(cell);
	}
	
	boolean decryptBackwardCell(Cell cell) {
		return cryptoState.decryptBackwardCell(cell);
	}
	
	public void updateForwardDigest(RelayCell cell) {
		cryptoState.updateForwardDigest(cell);
	}
	
	public byte[] getForwardDigestBytes() {
		return cryptoState.getForwardDigestBytes();
	}
	
	byte[] createOnionSkin() {
		final byte[] yBytes = dhContext.getPublicKeyBytes();
		final HybridEncryption hybrid = new HybridEncryption();
		return hybrid.encrypt(yBytes, routerDescriptor.getOnionKey());
	}
	
	private void deriveKeys(byte[] sharedSecret) {
		byte[] keyMaterial = new byte[TorMessageDigest.TOR_DIGEST_SIZE * 5];
		int offset = 0;
		byte[] buffer = new byte[sharedSecret.length + 1];
		System.arraycopy(sharedSecret, 0, buffer, 0, sharedSecret.length);
		for(int i = 0; i < 5; i++) {
			final TorMessageDigest md = new TorMessageDigest();
			buffer[sharedSecret.length] = (byte)i;
			md.update(buffer);
			System.arraycopy(md.getDigestBytes(), 0, keyMaterial, offset, TorMessageDigest.TOR_DIGEST_SIZE);
			offset += TorMessageDigest.TOR_DIGEST_SIZE;
		}
			
		cryptoState = CircuitNodeCryptoState.createFromKeyMaterial(keyMaterial);
	}
}
