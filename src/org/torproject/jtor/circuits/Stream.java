package org.torproject.jtor.circuits;

import java.io.InputStream;
import java.io.OutputStream;

public interface Stream {
	Circuit getCircuit();
	int getStreamId();
	void close();
	InputStream getInputStream();
	OutputStream getOutputStream();
}
