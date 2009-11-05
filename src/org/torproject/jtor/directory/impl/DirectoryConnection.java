package org.torproject.jtor.directory.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class DirectoryConnection {
	
	private final BufferedReader reader;
	private final BufferedWriter writer;
	
	DirectoryConnection(InputStream input, OutputStream output) {
		reader = new BufferedReader(new InputStreamReader(input));
		writer = new BufferedWriter(new OutputStreamWriter(output));
	}
	
	

}
