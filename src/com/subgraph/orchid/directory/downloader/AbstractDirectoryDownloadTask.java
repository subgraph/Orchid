package com.subgraph.orchid.directory.downloader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import com.subgraph.orchid.Directory;
import com.subgraph.orchid.OpenFailedException;
import com.subgraph.orchid.Stream;
import com.subgraph.orchid.TorException;
import com.subgraph.orchid.data.HexDigest;
import com.subgraph.orchid.directory.parsing.DocumentParserFactory;


public abstract class AbstractDirectoryDownloadTask implements Runnable {
	protected final static Logger logger = Logger.getLogger(AbstractDirectoryDownloadTask.class.getName());
	private final static boolean USE_COMPRESSION = true;
	
	private final DirectoryDownloader downloader;
	private final int purposeCode;
	
	protected AbstractDirectoryDownloadTask(DirectoryDownloader downloader, int purposeCode) {
		this.downloader = downloader;
		this.purposeCode = purposeCode;
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
		ByteBuffer response = null;
		try {
			logger.fine("request to "+ http.getHost() + " : "+ request);
			response = requestDocument(http, request);
			processResponse(response, http);
		} catch(IOException e) {
			logger.warning("IO error making request "+ request +" to host ["+ http.getHost() + "]: "+ e);
		} finally {
			http.close();
		}
	}
	
	abstract protected String getRequestPath();
	abstract protected void processResponse(ByteBuffer response, HttpConnection http);
	abstract protected void finishRequest(DirectoryDownloader downloader);
	
	protected ByteBuffer requestDocument(HttpConnection connection, String request) throws IOException {
		if(USE_COMPRESSION) {
			request += ".z";
		}
		connection.sendGetRequest(request);
		connection.readResponse();
		if(connection.getStatusCode() == 200) {
			return connection.getMessageBody();
		}
		throw new TorException("Request "+ request +" to directory "+ 
				connection.getHost() +" returned error code: "+ 
				connection.getStatusCode() + " "+ connection.getStatusMessage());
	}
	
	protected String fingerprintsToRequestString(List<HexDigest> fingerprints, boolean useMicrodescriptors) {
		final StringBuilder sb = new StringBuilder();
		for(HexDigest fp: fingerprints) {
			if(useMicrodescriptors) {
				appendMicrodescriptor(sb, fp);
			} else {
				appendFingerprint(sb, fp);
			}
		}
		return sb.toString();
	}
	
	private void appendFingerprint(StringBuilder sb, HexDigest fp) {
		if(sb.length() > 0) {
			sb.append("+");
		}
		sb.append(fp.toString());
	}
	
	private void appendMicrodescriptor(StringBuilder sb, HexDigest md) {
		if(sb.length() > 0) {
			sb.append("-");
		}
		sb.append(md.toBase64(true));
	}
}
