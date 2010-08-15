package org.torproject.jtor.circuits;

public interface OpenStreamResponse {
	enum OpenStreamStatus {
		STATUS_STREAM_OPENED,
		STATUS_STREAM_TIMEOUT,
		STATUS_STREAM_ERROR
	};

	OpenStreamStatus getStatus();
	Stream getStream();
	/**
	 * The 'reason' code from a RELAY_END cell sent in response to a RELAY_BEGIN or RELAY_BEGIN_DIR
	 * 
	 * @return The error 'reason' from a RELAY_END cell.
	 */
	int getErrorCode();
	
	String getErrorCodeMessage();
}
