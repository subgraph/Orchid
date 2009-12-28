package org.torproject.jtor.circuits.impl;

import org.torproject.jtor.circuits.Stream;
import org.torproject.jtor.circuits.OpenStreamResponse;

public class OpenStreamResponseImpl implements OpenStreamResponse {

	static OpenStreamResponse createStreamOpened(Stream stream) {
		return new OpenStreamResponseImpl(stream, OpenStreamStatus.STATUS_STREAM_OPENED);
	}
	
	static OpenStreamResponse createStreamError() {
		return new OpenStreamResponseImpl(null, OpenStreamStatus.STATUS_ERROR_OTHER);
	}
	
	private final Stream stream;
	private final OpenStreamStatus status;
	
	private OpenStreamResponseImpl(Stream stream, OpenStreamStatus status) {
		this.stream = stream;
		this.status = status;
	}
	
	public Stream getStream() {
		return stream;
	}

	public OpenStreamStatus getStatus() {
		return status;
	}
}
