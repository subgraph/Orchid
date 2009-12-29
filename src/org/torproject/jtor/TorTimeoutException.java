package org.torproject.jtor;

public class TorTimeoutException extends TorException {
	private static final long serialVersionUID = 1L;
	public TorTimeoutException(String message) {
		super(message);
	}
}
