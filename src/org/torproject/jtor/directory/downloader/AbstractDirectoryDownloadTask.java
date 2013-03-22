package org.torproject.jtor.directory.downloader;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.torproject.jtor.TorException;
import org.torproject.jtor.circuits.Circuit;
import org.torproject.jtor.circuits.OpenStreamResponse;
import org.torproject.jtor.data.HexDigest;
import org.torproject.jtor.directory.Directory;
import org.torproject.jtor.directory.Router;
import org.torproject.jtor.directory.parsing.DocumentParserFactory;


public abstract class AbstractDirectoryDownloadTask implements Runnable {
	protected final static Logger logger = Logger.getLogger("directory-download");
	private final static boolean USE_COMPRESSION = true;
	
	private final DirectoryDownloader downloader;
	private final int bootstrapRequestEvent;
	private final int bootstrapLoadingEvent;
	
	protected AbstractDirectoryDownloadTask(DirectoryDownloader downloader, int requestEvent, int loadingEvent) {
		this.downloader = downloader;
		this.bootstrapRequestEvent = requestEvent;
		this.bootstrapLoadingEvent = loadingEvent;
		logger.setLevel(Level.INFO);
	}

	protected Directory getDirectory() {
		return downloader.getDirectory();
	}

	protected DocumentParserFactory getParserFactory() {
		return downloader.getDocumentParserFactory();
	}

	protected HttpConnection openDirectoryConnection() {
		while(true) {
			final Router dir = getDirectory().getRandomDirectoryServer();
			final String hostname = createHostnameString(dir);
			final OpenStreamResponse osr = openDirectoryTo(dir);
			if(osr == null) {
				continue;
			}
			switch(osr.getStatus()) {
			case STATUS_STREAM_OPENED:
				logger.info("Directory stream opened to "+ hostname);
				return HttpConnection.createFromStream(hostname, osr.getStream());
			case STATUS_STREAM_ERROR:
				logger.warning("Error opening directory stream to "+ hostname +": ["+ 
						osr.getErrorCode() + "] "+ osr.getErrorCodeMessage());
				return null;
			case STATUS_STREAM_TIMEOUT:
				logger.warning("Timeout opening directory stream to "+ hostname);
				return null;
			}
		}
	}
	
	private OpenStreamResponse openDirectoryTo(Router directory) {
		try {
			final Circuit circuit = downloader.getCircuitManager().openDirectoryCircuitTo(directory);
			downloader.getCircuitManager().notifyInitializationEvent(bootstrapRequestEvent);
			return circuit.openDirectoryStream();
		} catch (TorException e) {
			logger.info("Failed connection to " + describeRouter(directory) + " : " + e.getMessage());
			return null;
		}
	}

	private String describeRouter(Router r) {
		return "'"+ r.getNickname() + "' ("+ r.getAddress().toString() + ":"+ r.getOnionPort() + ")";
	}
	public void run() {
		try {
			makeRequest();
		} finally {
			finishRequest(downloader);
		}
	}
	
	private void makeRequest() {
		final HttpConnection http = openDirectoryConnection();
		if(http == null) {
			return;
		}
		downloader.getCircuitManager().notifyInitializationEvent(bootstrapLoadingEvent);
		final String request = getRequestPath();
		Reader response = null;
		try {
			logger.info("request to "+ http.getHost() + " : "+ request);
			response = requestDocument(http, request);
			processResponse(response);
		} finally {
			closeReader(response);
			http.close();
		}
	}
	
	private void closeReader(Reader r) {
		try {
			if(r != null) {
				r.close();
			}
		} catch (IOException e) {
			logger.log(Level.WARNING, "Error closing directory reader "+ e.getMessage(), e);
		}
	}

	private String createHostnameString(Router r) {
		if(r.getOnionPort() == 80) {
			return r.getAddress().toString();
		} else {
			return r.getAddress().toString() + ":" + r.getOnionPort();
		}
	}
	
	abstract protected String getRequestPath();
	abstract protected void processResponse(Reader response);
	abstract protected void finishRequest(DirectoryDownloader downloader);
	
	protected Reader requestDocument(HttpConnection connection, String request) {
		if(USE_COMPRESSION) {
			request += ".z";
		}
		connection.sendGetRequest(request);
		connection.readResponse();
		if(connection.getStatusCode() == 200) {
			return connection.getBodyReader();
		}
		throw new TorException("Request "+ request +" to directory "+ 
				connection.getHost() +" returned error code: "+ 
				connection.getStatusCode() + " "+ connection.getStatusMessage());
	}
	
	protected String fingerprintsToRequestString(List<HexDigest> fingerprints) {
		final StringBuilder sb = new StringBuilder();
		for(HexDigest fp: fingerprints) {
			if(sb.length() > 0)
				sb.append("+");
			sb.append(fp.toString());
		}
		return sb.toString();
	}
}
