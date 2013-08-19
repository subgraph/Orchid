package com.subgraph.orchid.circuits.hs;

import java.nio.ByteBuffer;
import java.util.List;

import com.subgraph.orchid.Cell;
import com.subgraph.orchid.RelayCell;
import com.subgraph.orchid.Router;
import com.subgraph.orchid.circuits.CircuitBase;
import com.subgraph.orchid.circuits.CircuitManagerImpl;
import com.subgraph.orchid.circuits.path.CircuitPathChooser;
import com.subgraph.orchid.circuits.path.PathSelectionFailedException;
import com.subgraph.orchid.crypto.HybridEncryption;
import com.subgraph.orchid.crypto.TorPublicKey;

public class IntroductionCircuit extends CircuitBase {

	private final Router introductionRouter;
	private final IntroductionPoint introductionPoint;
	
	protected IntroductionCircuit(CircuitManagerImpl circuitManager, Router introductionRouter, IntroductionPoint introductionPoint) {
		super(circuitManager);
		this.introductionRouter = introductionRouter;
		this.introductionPoint = introductionPoint;
	}

	@Override
	protected List<Router> choosePath(CircuitPathChooser pathChooser)
			throws InterruptedException, PathSelectionFailedException {
		return pathChooser.choosePathWithFinal(introductionRouter);
	}
	
	boolean sendIntroduce(TorPublicKey permanentKey, byte[] publicKeyBytes, byte[] rendezvousCookie, Router rendezvousRouter) {
		final RelayCell introduceCell = createRelayCell(RelayCell.RELAY_COMMAND_INTRODUCE1, 0, getFinalCircuitNode());

		final byte[] payload = createIntroductionPayload(rendezvousRouter, publicKeyBytes, rendezvousCookie, permanentKey);
		final TorPublicKey serviceKey = introductionPoint.getServiceKey();
		introduceCell.putByteArray(serviceKey.getFingerprint().getRawBytes());
		introduceCell.putByteArray(payload);
		sendRelayCell(introduceCell);
		
		final RelayCell response = receiveRelayCell();
		if(response == null) {
			logger.info("Timeout waiting for response to INTRODUCE1 cell");
			return false;
		} else if(response.getRelayCommand() != RelayCell.RELAY_COMMAND_INTRODUCE_ACK) {
			logger.info("Unexpected relay cell type received waiting for response to INTRODUCE1 cell: "+ response.getRelayCommand());
			return false;
		} else if(response.cellBytesRemaining() == 0) {
			return true;
			
		} else {
			logger.info("INTRODUCE_ACK indicates that introduction was not forwarded: "+ response.getByte());
			return false;
		} 
	}
	
	private byte[] createIntroductionPayload(Router rendezvousRouter, byte[] publicKeyBytes, byte[] rendezvousCookie, TorPublicKey encryptionKey) {
		final ByteBuffer buffer = ByteBuffer.allocate(Cell.CELL_LEN);
		buffer.put((byte) 3);
		buffer.put((byte) 0);
		buffer.putInt((int) (System.currentTimeMillis() / 1000));
		buffer.put(rendezvousRouter.getAddress().getAddressDataBytes());
		buffer.putShort((short) rendezvousRouter.getOnionPort());
		buffer.put(rendezvousRouter.getIdentityHash().getRawBytes());
		TorPublicKey onionKey = rendezvousRouter.getOnionKey();
		byte[] rawBytes = onionKey.getRawBytes();
		buffer.putShort((short) rawBytes.length);
		buffer.put(rawBytes);
		buffer.put(rendezvousCookie);
		buffer.put(publicKeyBytes);
		int len = buffer.position();
		byte[] payload = new byte[len];
		buffer.flip();
		buffer.get(payload);
		HybridEncryption enc = new HybridEncryption();
		return enc.encrypt(payload, encryptionKey);
	}
}
