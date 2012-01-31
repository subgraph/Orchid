package org.torproject.jtor.circuits.impl;

import org.torproject.jtor.circuits.Stream;
import org.torproject.jtor.circuits.OpenStreamResponse;

public class OpenStreamResponseImpl implements OpenStreamResponse {

	static OpenStreamResponse createStreamOpened(Stream stream) {
		return new OpenStreamResponseImpl(stream, OpenStreamStatus.STATUS_STREAM_OPENED, 0);
	}
	
	static OpenStreamResponse createStreamTimeout() {
		return new OpenStreamResponseImpl(null, OpenStreamStatus.STATUS_STREAM_TIMEOUT, 0);
	}
	static OpenStreamResponse createStreamError(int reason) {
		return new OpenStreamResponseImpl(null, OpenStreamStatus.STATUS_STREAM_ERROR, reason);
	}
	
	private final int errorReason;
	private final Stream stream;
	private final OpenStreamStatus status;
	
	private OpenStreamResponseImpl(Stream stream, OpenStreamStatus status, int reason) {
		this.stream = stream;
		this.status = status;
		this.errorReason = reason;
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
		return RelayCellImpl.reasonToDescription(errorReason);
	}
}
