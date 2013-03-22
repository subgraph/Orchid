package org.torproject.jtor.circuits.impl;

import org.torproject.jtor.circuits.Stream;
import org.torproject.jtor.circuits.OpenStreamResponse;

public class OpenStreamResponseImpl implements OpenStreamResponse {

	private final static int CONNECTION_FAILED_REASON = -1;
	
	static OpenStreamResponse createStreamOpened(Stream stream) {
		return new OpenStreamResponseImpl(stream, OpenStreamStatus.STATUS_STREAM_OPENED, 0, null);
	}
	
	static OpenStreamResponse createStreamTimeout() {
		return new OpenStreamResponseImpl(null, OpenStreamStatus.STATUS_STREAM_TIMEOUT, 0, null);
	}
	static OpenStreamResponse createStreamError(int reason) {
		return new OpenStreamResponseImpl(null, OpenStreamStatus.STATUS_STREAM_ERROR, reason, null);
	}
	
	static OpenStreamResponse createConnectionFailError(String message) {
		return new OpenStreamResponseImpl(null, OpenStreamStatus.STATUS_STREAM_ERROR, CONNECTION_FAILED_REASON, message);
	}
	
	private final int errorReason;
	private final String connectionErrorMessage;
	private final Stream stream;
	private final OpenStreamStatus status;
	
	private OpenStreamResponseImpl(Stream stream, OpenStreamStatus status, int reason, String connFailMessage) {
		this.stream = stream;
		this.status = status;
		this.errorReason = reason;
		this.connectionErrorMessage = connFailMessage;
	}
	
	public Stream getStream() {
		return stream;
	}

	public OpenStreamStatus getStatus() {
		return status;
	}

	public int getErrorCode() {
		return errorReason;
	}

	public String getErrorCodeMessage() {
		if(errorReason == CONNECTION_FAILED_REASON) {
			return connectionErrorMessage;
		}
		return RelayCellImpl.reasonToDescription(errorReason);
	}
}
