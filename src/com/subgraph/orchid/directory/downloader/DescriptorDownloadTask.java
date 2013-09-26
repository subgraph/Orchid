package com.subgraph.orchid.directory.downloader;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.subgraph.orchid.CircuitManager;
import com.subgraph.orchid.RouterDescriptor;
import com.subgraph.orchid.RouterMicrodescriptor;
import com.subgraph.orchid.data.HexDigest;
import com.subgraph.orchid.directory.parsing.DocumentParser;
import com.subgraph.orchid.directory.parsing.DocumentParsingResultHandler;

public class DescriptorDownloadTask extends AbstractDirectoryDownloadTask{

	private final List<HexDigest> fingerprints;
	private final boolean useMicrodescriptors;
	
	DescriptorDownloadTask(List<HexDigest> fingerprints, DirectoryDownloader downloader, boolean useMicrodescriptors) {
		super(downloader, CircuitManager.DIRECTORY_PURPOSE_DESCRIPTORS);
		this.fingerprints = fingerprints;
		this.useMicrodescriptors = useMicrodescriptors;
	}

	@Override
	protected String getRequestPath() {
		final String fps = fingerprintsToRequestString(fingerprints, useMicrodescriptors);
		if(useMicrodescriptors) {
			return "/tor/micro/d/" + fps;
		} else {
			return "/tor/server/d/" + fps;
		}
	}
	
	@Override
	protected void processResponse(ByteBuffer response, final HttpConnection http) {
		if(useMicrodescriptors) {
			processMicrodescriptorResponse(response, http);
		} else {
			processDescriptorResponse(response, http);
		}
	}
	
	private void processDescriptorResponse(ByteBuffer response, final HttpConnection http) {
		final Set<HexDigest> requested = new HashSet<HexDigest>();
		requested.addAll(fingerprints);
		
		final DocumentParser<RouterDescriptor> parser = getParserFactory().createRouterDescriptorParser(response, true);
		final boolean success = parser.parse(new DocumentParsingResultHandler<RouterDescriptor>() {
			
			public void parsingError(String message) {
				logger.warning("Parsing error processing router descriptors from ["+ http.getHost() +"]: "+ message);
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
	
	private void processMicrodescriptorResponse(ByteBuffer response, final HttpConnection http) {
		final Set<HexDigest> requested = new HashSet<HexDigest>();
		requested.addAll(fingerprints);
		final DocumentParser<RouterMicrodescriptor> parser = getParserFactory().createRouterMicrodescriptorParser(response);
		final List<RouterMicrodescriptor> microdescriptors = new ArrayList<RouterMicrodescriptor>();
		boolean success = parser.parse(new DocumentParsingResultHandler<RouterMicrodescriptor>() {

			public void parsingError(String message) {
				logger.warning("Parsing error processing microdescriptor from ["+ http.getHost() + "]: "+ message);
			}

			public void documentParsed(RouterMicrodescriptor document) {
				if(!requested.contains(document.getDescriptorDigest())) {
					logger.warning("Server returned a router microdescriptor that was not requested.  Ignoring.");
					return;
				}
				microdescriptors.add(document);
			}

			public void documentInvalid(RouterMicrodescriptor document,	String message) {
				logger.warning("Invalid router microdescriptor returned: "+ message);
			}
		});
	
		if(success) {
			getDirectory().addRouterMicrodescriptors(microdescriptors);
		}
	}

	@Override
	protected void finishRequest(DirectoryDownloader downloader) {
		downloader.clearDownloadingDescriptors();
	}
}
