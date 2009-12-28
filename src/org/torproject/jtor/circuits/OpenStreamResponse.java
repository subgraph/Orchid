package org.torproject.jtor.circuits;

public interface OpenStreamResponse {
	enum OpenStreamStatus {
		STATUS_STREAM_OPENED,
		STATUS_ERROR_CONNECTION_REFUSED,
		STATUS_ERROR_OTHER
	};

	OpenStreamStatus getStatus();
	Stream getStream();
}
