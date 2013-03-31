package org.torproject.jtor.directory.downloader;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.torproject.jtor.TorException;
import org.torproject.jtor.circuits.OpenStreamResponse;
import org.torproject.jtor.circuits.Stream;
import org.torproject.jtor.data.HexDigest;
import org.torproject.jtor.directory.Directory;
import org.torproject.jtor.directory.Router;
import org.torproject.jtor.directory.parsing.DocumentParserFactory;


public abstract class AbstractDirectoryDownloadTask implements Runnable {
	protected final static Logger logger = Logger.getLogger(AbstractDirectoryDownloadTask.class.getName());
	private final static boolean USE_COMPRESSION = true;
	
	private final DirectoryDownloader downloader;
	private final int purposeCode;
	
	protected AbstractDirectoryDownloadTask(DirectoryDownloader downloader, int purposeCode) {
		this.downloader = downloader;
		this.purposeCode = purposeCode;
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
			final OpenStreamResponse osr = downloader.getCircuitManager().openDirectoryStream(purposeCode);
			if(osr == null) {
				continue;
			}
			switch(osr.getStatus()) {
			case STATUS_STREAM_OPENED:
				final String hostname = createHostnameStringFromStream(osr.getStream());
				logger.info("Directory stream opened to "+ hostname);
				return HttpConnection.createFromStream(hostname, osr.getStream());
			case STATUS_STREAM_ERROR:
				logger.warning("Error opening directory stream: ["+ 
						osr.getErrorCode() + "] "+ osr.getErrorCodeMessage());
				return null;
			case STATUS_STREAM_TIMEOUT:
				logger.warning("Timeout opening directory stream");
				return null;
			}
		}
	}
	
	public void run() {
		try {
			makeRequest();
		} catch(TorException e) { 
			logger.info(e.getMessage());
		} finally {
			finishRequest(downloader);
		}
	}
	
	private void makeRequest() {
		final HttpConnection http = openDirectoryConnection();
		if(http == null) {
			return;
		}
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

	private String createHostnameStringFromStream(Stream stream) {
		final Router r = stream.getCircuit().getFinalCircuitNode().getRouter();
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
