package com.subgraph.orchid.circuits.hs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import com.subgraph.orchid.Circuit;
import com.subgraph.orchid.CircuitBuildHandler;
import com.subgraph.orchid.CircuitNode;
import com.subgraph.orchid.Connection;
import com.subgraph.orchid.ConnectionCache;
import com.subgraph.orchid.Stream;
import com.subgraph.orchid.StreamConnectFailedException;
import com.subgraph.orchid.circuits.CircuitBuildTask;
import com.subgraph.orchid.circuits.CircuitCreationRequest;
import com.subgraph.orchid.circuits.CircuitManagerImpl;
import com.subgraph.orchid.circuits.path.CircuitPathChooser;
import com.subgraph.orchid.directory.DocumentFieldParserImpl;
import com.subgraph.orchid.directory.downloader.HttpConnection;
import com.subgraph.orchid.directory.parsing.DocumentParsingResultHandler;

public class HSDescriptorDownloader {
	private final static Logger logger = Logger.getLogger(HSDescriptorDirectory.class.getName());
	
	private final ConnectionCache connectionCache;
	private final CircuitManagerImpl circuitManager;
	private final CircuitPathChooser pathChooser;
	private final HiddenService hiddenService;
	private final List<HSDescriptorDirectory> directories;
	
	public HSDescriptorDownloader(ConnectionCache connectionCache, CircuitManagerImpl circuitManager, CircuitPathChooser pathChooser, HiddenService hiddenService, List<HSDescriptorDirectory> directories) {
		this.connectionCache = connectionCache;
		this.circuitManager = circuitManager;
		this.pathChooser = pathChooser;
		this.hiddenService = hiddenService;
		this.directories = directories;
	}

	
	public HSDescriptor downloadDescriptor() {
		for(HSDescriptorDirectory d: directories) {
			HSDescriptor descriptor = downloadDescriptorFrom(d);
			if(descriptor != null) {
				return descriptor;
			}
		}
		// All directories failed
		return null;
	}
	
	private HSDescriptor downloadDescriptorFrom(HSDescriptorDirectory directory) {
		logger.warning("Downloading descriptor from "+ directory);
		final HSDirectoryCircuit circuit = new HSDirectoryCircuit(circuitManager, directory.getDirectory());
		final HSCircuitResult result = new HSCircuitResult();
		
		CircuitCreationRequest request = new CircuitCreationRequest(pathChooser, circuit, result);
		CircuitBuildTask task = new CircuitBuildTask(request, connectionCache, null);
		task.run();
		if(!result.isSuccessful()) {
			return null;
		}
		try {
			Stream stream = circuit.openDirectoryStream(10000);
			HttpConnection http = new HttpConnection(stream);
			http.sendGetRequest("/tor/rendezvous2/"+ directory.getDescriptorId().toBase32());
			http.readResponse();
			if(http.getStatusCode() == 200) {
				readDocument(http.getBodyReader());
			} else {
				
			}
			
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (TimeoutException e) {
			circuit.markForClose();
			return null;
		} catch (StreamConnectFailedException e) {
			circuit.markForClose();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	private void readDocument(Reader reader) {
		DocumentFieldParserImpl fieldParser = new DocumentFieldParserImpl(reader);
		HSDescriptorParser parser = new HSDescriptorParser(fieldParser);
		DescriptorParseResult result = new DescriptorParseResult();
		parser.parse(result);
		System.out.println("done...");

		
	}
	
	private static class DescriptorParseResult implements DocumentParsingResultHandler<HSDescriptor> {
		HSDescriptor descriptor;
		HSDescriptor getDescriptor() {
			return descriptor;
		}
		public void documentParsed(HSDescriptor document) {
			this.descriptor = document;
		}

		public void documentInvalid(HSDescriptor document, String message) {
			System.out.println("Invalid document: "+ message);
			// TODO Auto-generated method stub
			
		}

		public void parsingError(String message) {
			System.out.println("Parsing error: "+ message);
		}
		
	}
	private static class HSCircuitResult implements CircuitBuildHandler {
		private boolean isFailed;

		public void connectionCompleted(Connection connection) {}
		public void nodeAdded(CircuitNode node) {}
		public void circuitBuildCompleted(Circuit circuit) {}		
		
		public void connectionFailed(String reason) {
			isFailed = true;
		}

		public void circuitBuildFailed(String reason) {
			isFailed = true;
		}
		
		boolean isSuccessful() {
			return !isFailed;
		}
	}
}