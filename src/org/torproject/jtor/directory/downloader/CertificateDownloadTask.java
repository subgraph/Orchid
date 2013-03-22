package org.torproject.jtor.directory.downloader;

import java.io.Reader;
import java.util.List;

import org.torproject.jtor.Tor;
import org.torproject.jtor.data.HexDigest;
import org.torproject.jtor.directory.KeyCertificate;
import org.torproject.jtor.directory.parsing.DocumentParser;
import org.torproject.jtor.directory.parsing.DocumentParsingResultHandler;

public class CertificateDownloadTask extends AbstractDirectoryDownloadTask{

	private final List<HexDigest> fingerprints;
	
	
	CertificateDownloadTask(List<HexDigest> fingerprints, DirectoryDownloader downloader) {
		super(downloader, Tor.BOOTSTRAP_STATUS_REQUESTING_KEYS, Tor.BOOTSTRAP_STATUS_LOADING_KEYS);
		this.fingerprints = fingerprints;
	}
    
	@Override
	protected String getRequestPath() {
		final String fps = fingerprintsToRequestString(fingerprints);
		return "/tor/keys/fp/"+ fps;
	}
	
	@Override
	protected void processResponse(Reader response) {
		final DocumentParser<KeyCertificate> parser = getParserFactory().createKeyCertificateParser(response);
		final boolean success = parser.parse(new DocumentParsingResultHandler<KeyCertificate>() {
			
			public void parsingError(String message) {
				logger.warning("Parsing error processing certificate document: "+ message);
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
