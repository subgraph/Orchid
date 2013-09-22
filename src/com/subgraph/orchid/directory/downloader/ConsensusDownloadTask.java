package com.subgraph.orchid.directory.downloader;

import java.io.Reader;

import com.subgraph.orchid.CircuitManager;
import com.subgraph.orchid.ConsensusDocument;
import com.subgraph.orchid.directory.parsing.DocumentParser;
import com.subgraph.orchid.directory.parsing.DocumentParsingResultHandler;

public class ConsensusDownloadTask extends AbstractDirectoryDownloadTask {

	
	private ConsensusDocument newConsensusDocument = null;
	private final boolean useMicrodescriptors;
	
	ConsensusDownloadTask(DirectoryDownloader downloader, boolean useMicrodescriptors) {
		super(downloader, CircuitManager.DIRECTORY_PURPOSE_CONSENSUS);
		this.useMicrodescriptors = useMicrodescriptors;
	}

	@Override
	protected String getRequestPath() {
		if(useMicrodescriptors) {
			return "/tor/status-vote/current/consensus-microdesc";
		} else {
			return "/tor/status-vote/current/consensus";
		}
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
				getDirectory().addConsensusDocument(document, false);
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
