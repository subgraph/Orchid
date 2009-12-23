package org.torproject.jtor.circuits;

import java.io.InputStream;
import java.io.OutputStream;

public interface Stream {
	void close();
	InputStream getInputStream();
	OutputStream getOutputStream();
}
