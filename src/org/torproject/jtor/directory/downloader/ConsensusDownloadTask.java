package org.torproject.jtor.directory.downloader;

import java.io.Reader;

import org.torproject.jtor.Tor;
import org.torproject.jtor.directory.ConsensusDocument;
import org.torproject.jtor.directory.parsing.DocumentParser;
import org.torproject.jtor.directory.parsing.DocumentParsingResultHandler;

public class ConsensusDownloadTask extends AbstractDirectoryDownloadTask {

	ConsensusDownloadTask(DirectoryDownloader downloader) {
		super(downloader, Tor.BOOTSTRAP_STATUS_REQUESTING_STATUS, Tor.BOOTSTRAP_STATUS_LOADING_STATUS);
	}

	@Override
	protected String getRequestPath() {
		return "/tor/status-vote/current/consensus";
	}
	
	@Override
	protected void processResponse(Reader response) {
		final DocumentParser<ConsensusDocument> parser = getParserFactory().createConsensusDocumentParser(response);
		final boolean success = parser.parse(new DocumentParsingResultHandler<ConsensusDocument>() {
			
			public void parsingError(String message) {
				logger.warning("Parsing error processing consensus document: "+ message);
			}
			
			public void documentParsed(ConsensusDocument document) {
				getDirectory().addConsensusDocument(document);
			}
			
			public void documentInvalid(ConsensusDocument document, String message) {
				logger.warning("Received consensus document is invalid: "+ message);
			}
		});
	
		if(success) {
			getDirectory().storeConsensus();
		}
	}

	@Override
	protected void finishRequest(DirectoryDownloader downloader) {
		downloader.clearDownloadingConsensus();
	}
}
