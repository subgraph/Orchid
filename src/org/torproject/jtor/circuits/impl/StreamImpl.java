package org.torproject.jtor.circuits.impl;

import java.io.InputStream;
import java.io.OutputStream;

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
	private boolean isClosed;
	
	StreamImpl(CircuitImpl circuit, CircuitNode targetNode, int streamId) {
		this.circuit = circuit;
		this.targetNode = targetNode;
		this.streamId = streamId;
		this.inputStream = new TorInputStream(this);
		this.outputStream = new TorOutputStream(this);
	}

	void addInputCell(RelayCell cell) {
		if(isClosed)
			return;
		if(cell.getRelayCommand() == RelayCell.RELAY_END)
			inputStream.addEndCell(cell);
		else
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
		if(isClosed)
			return;
		isClosed = true;
		inputStream.close();
		outputStream.close();
		// XXX when to remove?
		//circuit.removeStream(this);
	}

	void openDirectory() {
		final RelayCell cell = new RelayCellImpl(circuit.getFinalCircuitNode(), circuit.getCircuitId(), streamId, RelayCell.RELAY_BEGIN_DIR);
		circuit.sendRelayCellToFinalNode(cell);
		receiveRelayConnectedCell();
	}

	void openExit(String target, int port) {
		final RelayCell cell = new RelayCellImpl(circuit.getFinalCircuitNode(), circuit.getCircuitId(), streamId, RelayCell.RELAY_BEGIN);
		cell.putString(target + ":"+ port);
		circuit.sendRelayCellToFinalNode(cell);
		receiveRelayConnectedCell();
	}

	private RelayCell receiveRelayConnectedCell() {
		final RelayCell responseCell = circuit.receiveRelayCell();
		final int command = responseCell.getRelayCommand();
		if(command != RelayCell.RELAY_CONNECTED)
			throw new TorException("Did not receive expected RELAY_CONNECTED cell.  cell = "+ responseCell);

		if(responseCell.getStreamId() != streamId) {
			circuit.removeStream(this);
			throw new TorException("Did not receive expected stream id");
		}
		return responseCell;
	}
	public InputStream getInputStream() {
		return inputStream;
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}
}
