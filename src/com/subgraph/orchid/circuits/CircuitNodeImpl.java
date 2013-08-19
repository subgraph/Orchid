package com.subgraph.orchid.circuits;

import java.math.BigInteger;

import com.subgraph.orchid.Cell;
import com.subgraph.orchid.CircuitNode;
import com.subgraph.orchid.RelayCell;
import com.subgraph.orchid.Router;
import com.subgraph.orchid.TorException;
import com.subgraph.orchid.crypto.HybridEncryption;
import com.subgraph.orchid.crypto.TorCreateFastKeyAgreement;
import com.subgraph.orchid.crypto.TorKeyAgreement;
import com.subgraph.orchid.crypto.TorMessageDigest;
import com.subgraph.orchid.data.HexDigest;

public class CircuitNodeImpl implements CircuitNode {
	static CircuitNodeImpl createForRouter(Router router) {
		return new CircuitNodeImpl(router);
	}

	private final static int CIRCWINDOW_START = 1000;
	private final static int CIRCWINDOW_INCREMENT = 100;
	private final TorKeyAgreement dhContext;
	private final Router router;
	private CircuitNodeCryptoState cryptoState;
	private final CircuitNodeImpl previousNode;

	private final Object windowLock;
	private int packageWindow;
	private int deliverWindow;
	
	private TorCreateFastKeyAgreement createFastContext;

	private CircuitNodeImpl(Router router) {
		this(router, null);
	}

	protected CircuitNodeImpl(Router router, CircuitNodeImpl previous) {
		previousNode = previous;
		this.router = router;
		this.dhContext = new TorKeyAgreement();
		windowLock = new Object();
		packageWindow = CIRCWINDOW_START;
		deliverWindow = CIRCWINDOW_START;
	}

	public Router getRouter() {
		return router;
	}

	void setCreatedFastValue(byte[] value, HexDigest packetDigest) {
		createFastContext.setOtherValue(value);
		deriveKeys(createFastContext.getDerivedValue());
		if(!cryptoState.verifyPacketDigest(packetDigest)) {
			throw new TorException("Digest verification failed");
		}
	}
	
	public void setSharedSecret(BigInteger peerPublic, HexDigest packetDigest) {
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

	protected TorKeyAgreement getKeyAgreement() {
		return dhContext;
	}
	
	byte[] createOnionSkin() {
		final byte[] yBytes = dhContext.getPublicKeyBytes();
		final HybridEncryption hybrid = new HybridEncryption();
		return hybrid.encrypt(yBytes, router.getOnionKey());
	}

	byte [] getCreateFastPublicValue() {
		if(createFastContext == null) {
			createFastContext = new TorCreateFastKeyAgreement();
		}
		return createFastContext.getPublicValue();
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

	public String toString() {
		return "|"+ router.getNickname() + "|";
	}

	public void decrementDeliverWindow() {
		synchronized(windowLock) {
			deliverWindow--;
		}
	}

	public boolean considerSendingSendme() {
		synchronized(windowLock) {
			if(deliverWindow <= (CIRCWINDOW_START - CIRCWINDOW_INCREMENT)) {
				deliverWindow += CIRCWINDOW_INCREMENT;
				return true;
			}
			return false;
		}
	}

	public void waitForSendWindow() {
		waitForSendWindow(false);
	}

	public void waitForSendWindowAndDecrement() {
		waitForSendWindow(true);
	}

	private void waitForSendWindow(boolean decrement) {
		synchronized(windowLock) {
			while(packageWindow == 0) {
				try {
					windowLock.wait();
				} catch (InterruptedException e) {
					throw new TorException("Thread interrupted while waiting for circuit send window");
				}
			}
			if(decrement)
				packageWindow--;
		}
	}

	public void incrementSendWindow() {
		synchronized(windowLock) {
			packageWindow += CIRCWINDOW_INCREMENT;
			windowLock.notifyAll();
		}
		
	}
}
