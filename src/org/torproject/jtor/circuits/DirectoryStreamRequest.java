package org.torproject.jtor.circuits;

import org.torproject.jtor.directory.Router;

public class DirectoryStreamRequest {
	
	private final Router directory;
	private int requestEvent = 0;
	private int loadingEvent = 0;
	
	public DirectoryStreamRequest(Router directory) {
		this.directory = directory;
	}
	
	public void setInitializationEvents(int requestEvent, int loadingEvent) {
		this.requestEvent = requestEvent;
		this.loadingEvent = loadingEvent;
	}
	
	public Router getDirectoryRouter() {
		return directory;
	}

	public int getRequestEventCode() {
		return requestEvent;
	}
	
	public int getLoadingEventCode() {
		return loadingEvent;
	}
}
