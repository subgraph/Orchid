package org.torproject.jtor.circuits.impl;

import org.torproject.jtor.circuits.cells.Cell;
import org.torproject.jtor.circuits.cells.RelayCell;
import org.torproject.jtor.crypto.TorMessageDigest;
import org.torproject.jtor.crypto.TorStreamCipher;
import org.torproject.jtor.data.HexDigest;

class CircuitNodeCryptoState {
	static CircuitNodeCryptoState createFromKeyMaterial(byte[] keyMaterial) {
		return new CircuitNodeCryptoState(keyMaterial);
	}
	
	private final HexDigest checksumDigest;
	private final TorMessageDigest forwardDigest;
	private final TorMessageDigest backwardDigest;
	private final TorStreamCipher forwardCipher;
	private final TorStreamCipher backwardCipher;
	
	static private byte[] extractDigestBytes(byte[] keyMaterial, int offset) {
		final byte[] digestBytes = new byte[TorMessageDigest.TOR_DIGEST_SIZE];
		System.arraycopy(keyMaterial, offset, digestBytes, 0, TorMessageDigest.TOR_DIGEST_SIZE);
		return digestBytes;
	}
	
	static private byte[] extractCipherKey(byte[] keyMaterial, int offset) {
		final byte[] keyBytes = new byte[TorStreamCipher.KEY_LEN];
		System.arraycopy(keyMaterial, offset, keyBytes, 0, TorStreamCipher.KEY_LEN);
		return keyBytes;
	}
	
	private CircuitNodeCryptoState(byte[] keyMaterial) {
		int offset = 0;
		checksumDigest = HexDigest.createFromDigestBytes(extractDigestBytes(keyMaterial, offset));
		offset += TorMessageDigest.TOR_DIGEST_SIZE;
		
		forwardDigest = new TorMessageDigest();
		forwardDigest.update(extractDigestBytes(keyMaterial, offset));
		offset += TorMessageDigest.TOR_DIGEST_SIZE;

		backwardDigest = new TorMessageDigest();
		backwardDigest.update(extractDigestBytes(keyMaterial, offset));
		offset += TorMessageDigest.TOR_DIGEST_SIZE;
		
		forwardCipher = TorStreamCipher.createFromKeyBytes(extractCipherKey(keyMaterial, offset));
		offset += TorStreamCipher.KEY_LEN;
		
		backwardCipher = TorStreamCipher.createFromKeyBytes(extractCipherKey(keyMaterial, offset));
	}
	
	boolean verifyPacketDigest(HexDigest packetDigest) {
		return checksumDigest.equals(packetDigest);
	}
	
	void encryptForwardCell(Cell cell) {
		forwardCipher.encrypt(cell.getCellBytes(), Cell.CELL_HEADER_LEN, Cell.CELL_PAYLOAD_LEN);
	}
	
	boolean decryptBackwardCell(Cell cell) {
		backwardCipher.encrypt(cell.getCellBytes(), Cell.CELL_HEADER_LEN, Cell.CELL_PAYLOAD_LEN);
		return isRecognizedCell(cell);
	}
	
	void updateForwardDigest(Cell cell) {
		forwardDigest.update(cell.getCellBytes(), Cell.CELL_HEADER_LEN, Cell.CELL_PAYLOAD_LEN);
	}
	
	byte[] getForwardDigestBytes() {
		return forwardDigest.getDigestBytes();
	}
	
	private boolean isRecognizedCell(Cell cell) {
		if(cell.getShortAt(RelayCell.RECOGNIZED_OFFSET) != 0) 
			return false;
	
		final byte[] digest = extractRelayDigest(cell);
		final byte[] peek = backwardDigest.peekDigest(cell.getCellBytes(), Cell.CELL_HEADER_LEN, Cell.CELL_PAYLOAD_LEN);
		for(int i = 0; i < 4; i++) 
			if(digest[i] != peek[i]) {
				replaceRelayDigest(cell, digest);
				return false;
			}
		backwardDigest.update(cell.getCellBytes(), Cell.CELL_HEADER_LEN, Cell.CELL_PAYLOAD_LEN);
		replaceRelayDigest(cell, digest);
		return true;		
	}
	
	private byte[] extractRelayDigest(Cell cell) {
		final byte[] digest = new byte[4];
		for(int i = 0; i < 4; i++) {
			digest[i] = (byte) cell.getByteAt(i + RelayCell.DIGEST_OFFSET);
			cell.putByteAt(i + RelayCell.DIGEST_OFFSET, 0);
		}
		return digest;
	}
	
	private void replaceRelayDigest(Cell cell, byte[] digest) {
		for(int i = 0; i < 4; i++)
			cell.putByteAt(i + RelayCell.DIGEST_OFFSET, digest[i] & 0xFF);	
	}
	
	

}
