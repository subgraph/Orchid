package org.torproject.jtor.circuits;

import org.torproject.jtor.TorException;

public class ConnectionConnectException extends TorException {
	
	private static final long serialVersionUID = -6120800907993934051L;

	public ConnectionConnectException() {
		super();
	}
	
	public ConnectionConnectException(String message) {
		super(message);
	}

}
