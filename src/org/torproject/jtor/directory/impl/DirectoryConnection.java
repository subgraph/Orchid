package org.torproject.jtor.directory.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.torproject.jtor.TorException;
import org.torproject.jtor.data.HexDigest;

public class DirectoryConnection {
	private final static boolean USE_COMPRESSION = true;
	private final HttpConnection http;
	
	DirectoryConnection(String host, InputStream input, OutputStream output) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(input, "ISO-8859-1"));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, "ISO-8859-1"));
			http = new HttpConnection(host, reader, writer);
		} catch (UnsupportedEncodingException e) {
			throw new TorException("Character set ISO-8859-1 not supported (?!?)");
		}
	}
	
	Reader getCertificatesByFingerprint(List<HexDigest> fingerprints) {
		final String fps = fingerprintsToRequestString(fingerprints);
		return requestDocument("/tor/keys/fp/"+ fps);
	}
	
	Reader getConsensus() {
		return requestDocument("/tor/status-vote/current/consensus");
	}
	
	Reader getDescriptorsByDigests(List<HexDigest> digests) {
		final String fps = fingerprintsToRequestString(digests);
		return requestDocument("/tor/server/d/"+ fps);
	}
	
	private String fingerprintsToRequestString(List<HexDigest> fingerprints) {
		final StringBuilder sb = new StringBuilder();
		for(HexDigest fp: fingerprints) {
			if(sb.length() > 0)
				sb.append("+");
			sb.append(fp.toString());
		}
		return sb.toString();
	}
	public Reader requestDocument(String request) {
		if(USE_COMPRESSION)
			request = request + ".z";
		http.sendGetRequest(request);
		http.readResponse();
		if(http.getStatusCode() == 200) {
			return http.getBodyReader();
		} else {
			throw new TorException("HTTP server returned error code: "+ http.getStatusCode() +" "+ http.getStatusMessage());
		}
		
	}
	

}
