package org.torproject.jtor.circuits;

import org.torproject.jtor.TorException;

public class ConnectionClosedException extends TorException {
	
	private static final long serialVersionUID = 8511575134115330384L;

	public ConnectionClosedException() {
		super();
	}
	
	public ConnectionClosedException(String message) {
		super(message);
	}

}
