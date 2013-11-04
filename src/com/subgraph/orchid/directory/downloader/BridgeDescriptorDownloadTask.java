package com.subgraph.orchid.directory.downloader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import com.subgraph.orchid.CircuitManager;
import com.subgraph.orchid.DirectoryCircuit;
import com.subgraph.orchid.OpenFailedException;
import com.subgraph.orchid.Router;
import com.subgraph.orchid.RouterDescriptor;
import com.subgraph.orchid.Stream;
import com.subgraph.orchid.StreamConnectFailedException;
import com.subgraph.orchid.TorException;
import com.subgraph.orchid.directory.parsing.BasicDocumentParsingResult;
import com.subgraph.orchid.directory.parsing.DocumentParser;
import com.subgraph.orchid.directory.parsing.DocumentParserFactory;

public class BridgeDescriptorDownloadTask implements Callable<RouterDescriptor> {
	private final static Logger logger = Logger.getLogger(BridgeDescriptorDownloadTask.class.getName());
	
	private final DocumentParserFactory parserFactory;
	private final CircuitManager circuitManager;
	private final Router target;
	
	public BridgeDescriptorDownloadTask(DocumentParserFactory parserFactory, CircuitManager circuitManager, Router target) {
		this.parserFactory = parserFactory;
		this.circuitManager = circuitManager;
		this.target = target;
	}

	public RouterDescriptor call() throws OpenFailedException, IOException, InterruptedException, TimeoutException, StreamConnectFailedException, TorException {
		final HttpConnection connection = openConnection();
		try {
			final ByteBuffer body = requestDocument(connection);
			return processResponse(body);
		} finally {
			connection.close();
		}
	}
	
	private HttpConnection openConnection() throws OpenFailedException, InterruptedException, TimeoutException, StreamConnectFailedException {
		DirectoryCircuit circuit = circuitManager.openDirectoryCircuitTo(Arrays.asList(target));
		Stream stream = circuit.openDirectoryStream(2000);
		return new HttpConnection(stream);
	}

	private ByteBuffer requestDocument(HttpConnection connection) throws IOException {
		connection.sendGetRequest("/tor/server/authority");
		connection.readResponse();
		if(connection.getStatusCode() == 200) {
			return connection.getMessageBody();
		}
		throw new TorException("Request /tor/server/authority to bridge "+ target 
				+" returned error code: "+ connection.getStatusCode() +" "+ connection.getStatusMessage());
	}
	
	private RouterDescriptor processResponse(ByteBuffer body) {
		final DocumentParser<RouterDescriptor> parser = parserFactory.createRouterDescriptorParser(body, true);
		final BasicDocumentParsingResult<RouterDescriptor> result = new BasicDocumentParsingResult<RouterDescriptor>();
		if(parser.parse(result) && !result.isError()) {
			logger.fine("Valid descriptor received from bridge "+ target);
			return result.getDocument();
		}
		logger.warning("Failed to parse descriptor returned from bridge:  "+ target +" ("+ result.getMessage() + ")");
		return null;
	}
	
	
	
	
	
	

}
