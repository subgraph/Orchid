package org.torproject.jtor;

public interface TorInitializationListener {
	void initializationProgress(String message, int percent);
	void initializationCompleted();
}
