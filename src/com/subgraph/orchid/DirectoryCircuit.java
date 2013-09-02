package com.subgraph.orchid;

import java.util.concurrent.TimeoutException;

public interface DirectoryCircuit extends Circuit {
	/**
	 * Open an anonymous connection to the directory service running on the
	 * final node in this circuit.
	 * 
	 * @return The status response returned by trying to open the stream.
	 */
	Stream openDirectoryStream(long timeout) throws InterruptedException, TimeoutException, StreamConnectFailedException;
}
