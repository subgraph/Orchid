package com.subgraph.orchid.directory.downloader;

import java.io.Reader;
import java.util.List;

import com.subgraph.orchid.CircuitManager;
import com.subgraph.orchid.KeyCertificate;
import com.subgraph.orchid.data.HexDigest;
import com.subgraph.orchid.directory.parsing.DocumentParser;
import com.subgraph.orchid.directory.parsing.DocumentParsingResultHandler;

public class CertificateDownloadTask extends AbstractDirectoryDownloadTask{

	private final List<HexDigest> fingerprints;
	
	
	CertificateDownloadTask(List<HexDigest> fingerprints, DirectoryDownloader downloader) {
		super(downloader, CircuitManager.DIRECTORY_PURPOSE_CERTIFICATES);
		this.fingerprints = fingerprints;
	}
    
	@Override
	protected String getRequestPath() {
		final String fps = fingerprintsToRequestString(fingerprints);
		return "/tor/keys/fp/"+ fps;
	}
	
	@Override
	protected void processResponse(Reader response, final HttpConnection http) {
		final DocumentParser<KeyCertificate> parser = getParserFactory().createKeyCertificateParser(response);
		final boolean success = parser.parse(new DocumentParsingResultHandler<KeyCertificate>() {
			
			public void parsingError(String message) {
				logger.warning("Parsing error processing certificate document from ["+ http.getHost() +"]: "+ message);
			}
			
			public void documentParsed(KeyCertificate document) {
				getDirectory().addCertificate(document);
			}
			
			public void documentInvalid(KeyCertificate document, String message) {
				logger.warning("Received invalid certificate document: " + message);
			}
		});
	
		if(success) {
			getDirectory().storeCertificates();
		}
	}

	@Override
	protected void finishRequest(DirectoryDownloader downloader) {
		downloader.clearDownloadingCertificates();
	}
}
