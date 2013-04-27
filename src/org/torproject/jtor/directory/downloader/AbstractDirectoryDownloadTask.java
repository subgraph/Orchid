package org.torproject.jtor.directory.downloader;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.torproject.jtor.TorException;
import org.torproject.jtor.circuits.OpenFailedException;
import org.torproject.jtor.circuits.Stream;
import org.torproject.jtor.data.HexDigest;
import org.torproject.jtor.directory.Directory;
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

	protected HttpConnection openDirectoryConnection() throws InterruptedException, TimeoutException, OpenFailedException {
			final Stream stream = downloader.getCircuitManager().openDirectoryStream(purposeCode);
			return new HttpConnection(stream);
	}
	
	public void run() {
		try {
			makeRequest();
		} catch(TorException e) { 
			logger.info(e.getMessage());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (TimeoutException e) {
			logger.warning("Timeout opening directory stream");
		} catch (OpenFailedException e) { 
			logger.warning("Failed to open directory stream");
		} finally {
			finishRequest(downloader);
		}
	}
	
	private void makeRequest() throws InterruptedException, TimeoutException, OpenFailedException {
		final HttpConnection http = openDirectoryConnection();
		final String request = getRequestPath();
		Reader response = null;
		try {
			logger.fine("request to "+ http.getHost() + " : "+ request);
			response = requestDocument(http, request);
			processResponse(response, http);
		} catch(IOException e) {
			logger.warning("IO error making request "+ request +" to host ["+ http.getHost() + "]: "+ e);
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
	
	abstract protected String getRequestPath();
	abstract protected void processResponse(Reader response, HttpConnection http);
	abstract protected void finishRequest(DirectoryDownloader downloader);
	
	protected Reader requestDocument(HttpConnection connection, String request) throws IOException {
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
