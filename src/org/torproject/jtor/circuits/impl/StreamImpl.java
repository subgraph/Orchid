package org.torproject.jtor.circuits.impl;

import org.torproject.jtor.TorException;
import org.torproject.jtor.circuits.Circuit;
import org.torproject.jtor.circuits.CircuitNode;
import org.torproject.jtor.circuits.Stream;
import org.torproject.jtor.circuits.cells.RelayCell;

public class StreamImpl implements Stream {
	private final CircuitImpl circuit;
	private final int streamId;
	private final CircuitNode targetNode;
	private final TorInputStream inputStream;
	private final TorOutputStream outputStream;
	
	StreamImpl(CircuitImpl circuit, CircuitNode targetNode, int streamId) {
		this.circuit = circuit;
		this.targetNode = targetNode;
		this.streamId = streamId;
		this.inputStream = new TorInputStream(this);
		this.outputStream = new TorOutputStream(this);
	}
	
	void addInputCell(RelayCell cell) {
		inputStream.addInputCell(cell);
	}
	
	int getStreamId() {
		return streamId;
	}
	
	Circuit getCircuit() {
		return circuit;
	}
	
	CircuitNode getTargetNode() {
		return targetNode;
	}
	
	void close() {
		inputStream.close();
		outputStream.close();
		circuit.removeStream(this);
	}
	
	void openDirectory() {
		final RelayCell cell = new RelayCellImpl(circuit.getFinalCircuitNode(), circuit.getCircuitId(), streamId, RelayCell.RELAY_BEGIN_DIR);
		circuit.sendRelayCellToFinalNode(cell);
		final RelayCell responseCell = circuit.receiveRelayResponse(RelayCell.RELAY_CONNECTED);
		if(responseCell.getStreamId() != streamId) {
			circuit.removeStream(this);
			throw new TorException("Did not receive expected stream id");
		}			
	}
}
