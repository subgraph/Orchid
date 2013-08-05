package com.subgraph.orchid.directory.downloader;

import java.io.Reader;
import java.util.Set;

import com.subgraph.orchid.CircuitManager;
import com.subgraph.orchid.ConsensusDocument.RequiredCertificate;
import com.subgraph.orchid.KeyCertificate;
import com.subgraph.orchid.directory.parsing.DocumentParser;
import com.subgraph.orchid.directory.parsing.DocumentParsingResultHandler;

public class CertificateDownloadTask extends AbstractDirectoryDownloadTask{

	private final Set<RequiredCertificate> requiredCertificates;
	
	CertificateDownloadTask(Set<RequiredCertificate> requiredCertificates, DirectoryDownloader downloader) {
		super(downloader, CircuitManager.DIRECTORY_PURPOSE_CERTIFICATES);
		this.requiredCertificates = requiredCertificates;
	}
    
	@Override
	protected String getRequestPath() {
		return "/tor/keys/fp-sk/"+ getRequiredCertificatesRequestString();
	}
	
	private String getRequiredCertificatesRequestString() {
		final StringBuilder sb = new StringBuilder();
		for(RequiredCertificate rc: requiredCertificates) {
			if(sb.length() > 0) {
				sb.append("+");
			}
			sb.append(rc.getAuthorityIdentity().toString());
			sb.append("-");
			sb.append(rc.getSigningKey().toString());
		}
		return sb.toString();
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
