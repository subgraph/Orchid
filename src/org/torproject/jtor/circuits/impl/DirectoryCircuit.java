package org.torproject.jtor.circuits.impl;

import java.util.List;

import org.torproject.jtor.circuits.OpenStreamResponse;
import org.torproject.jtor.circuits.path.CircuitPathChooser;
import org.torproject.jtor.directory.Router;

public class DirectoryCircuit extends CircuitBase {

	protected DirectoryCircuit(CircuitManagerImpl circuitManager) {
		super(circuitManager);
	}
	
	boolean isDirectoryCircuit() {
		return true;
	}

	public OpenStreamResponse openDirectoryStream() {
		final StreamImpl stream = createNewStream();
		final OpenStreamResponse response = stream.openDirectory();
		processOpenStreamResponse(stream, response);
		return response;
	}

	@Override
	List<Router> choosePath(CircuitPathChooser pathChooser) {
		return pathChooser.chooseDirectoryPath();
	}
}
