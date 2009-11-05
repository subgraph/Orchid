package org.torproject.jtor.circuits;

import java.util.List;

import org.torproject.jtor.directory.RouterDescriptor;

public interface CircuitManager {
	Stream getDirectoryStream();
	Circuit createCircuitFromNicknames(List<String> nicknamePath);
	Circuit createCircuitFromPath(List<RouterDescriptor> path);
}
