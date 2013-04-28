package org.torproject.jtor.directory.downloader;

import java.io.Reader;

import org.torproject.jtor.CircuitManager;
import org.torproject.jtor.ConsensusDocument;
import org.torproject.jtor.directory.parsing.DocumentParser;
import org.torproject.jtor.directory.parsing.DocumentParsingResultHandler;

public class ConsensusDownloadTask extends AbstractDirectoryDownloadTask {

	private ConsensusDocument newConsensusDocument = null;
	
	ConsensusDownloadTask(DirectoryDownloader downloader) {
		super(downloader, CircuitManager.DIRECTORY_PURPOSE_CONSENSUS);
	}

	@Override
	protected String getRequestPath() {
		return "/tor/status-vote/current/consensus";
	}
	
	@Override
	protected void processResponse(Reader response, final HttpConnection http) {
		final DocumentParser<ConsensusDocument> parser = getParserFactory().createConsensusDocumentParser(response);
		final boolean success = parser.parse(new DocumentParsingResultHandler<ConsensusDocument>() {
			
			public void parsingError(String message) {
				logger.warning("Parsing error processing consensus document from ["+ http.getHost() +"]: "+ message);
			}
			
			public void documentParsed(ConsensusDocument document) {
				newConsensusDocument = document;
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
		if(newConsensusDocument != null) {
			downloader.setCurrentConsensus(newConsensusDocument);
		}
		downloader.clearDownloadingConsensus();
	}
}
