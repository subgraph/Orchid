package org.torproject.jtor.directory.downloader;

import java.io.Reader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.torproject.jtor.Tor;
import org.torproject.jtor.data.HexDigest;
import org.torproject.jtor.directory.RouterDescriptor;
import org.torproject.jtor.directory.parsing.DocumentParser;
import org.torproject.jtor.directory.parsing.DocumentParsingResultHandler;

public class DescriptorDownloadTask extends AbstractDirectoryDownloadTask{

	private final List<HexDigest> fingerprints;
	
	DescriptorDownloadTask(List<HexDigest> fingerprints, DirectoryDownloader downloader) {
		super(downloader, Tor.BOOTSTRAP_STATUS_REQUESTING_DESCRIPTORS, Tor.BOOTSTRAP_STATUS_LOADING_DESCRIPTORS);
		this.fingerprints = fingerprints;
	}

	@Override
	protected String getRequestPath() {
		final String fps = fingerprintsToRequestString(fingerprints);
		return "/tor/server/d/" + fps;
	}
	
	@Override
	protected void processResponse(Reader response) {
		final Set<HexDigest> requested = new HashSet<HexDigest>();
		requested.addAll(fingerprints);
		
		final DocumentParser<RouterDescriptor> parser = getParserFactory().createRouterDescriptorParser(response);
		final boolean success = parser.parse(new DocumentParsingResultHandler<RouterDescriptor>() {
			
			public void parsingError(String message) {
				logger.warning("Parsing error processing router descriptors: "+ message);
			}
			
			public void documentParsed(RouterDescriptor document) {
				if(!requested.contains(document.getDescriptorDigest())) {
					logger.warning("Server returned a router descriptor that was not requested.  Ignoring.");
					return;
				}
				getDirectory().addRouterDescriptor(document);
			}
			
			public void documentInvalid(RouterDescriptor document, String message) {
				logger.warning("Router descriptor "+ document.getNickname() +" invalid: "+ message);
				getDirectory().markDescriptorInvalid(document);
			}
		});
		
		if(success) {
			getDirectory().storeDescriptors();
		}
	}

	@Override
	protected void finishRequest(DirectoryDownloader downloader) {
		downloader.clearDownloadingDescriptors();
	}
}
