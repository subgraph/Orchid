package com.subgraph.orchid.directory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.subgraph.orchid.RouterMicrodescriptor;
import com.subgraph.orchid.RouterMicrodescriptor.CacheLocation;
import com.subgraph.orchid.Tor;
import com.subgraph.orchid.directory.parsing.DocumentParser;
import com.subgraph.orchid.directory.parsing.DocumentParserFactory;
import com.subgraph.orchid.directory.parsing.DocumentParsingResult;
import com.subgraph.orchid.directory.router.MicrodescriptorCacheLocation;

public class MicrodescriptorCacheLoader {
	private final static Logger logger = Logger.getLogger(MicrodescriptorCacheLoader.class.getName());
	
	private final static byte[] FIRST_DESCRIPTOR_KEYWORD = "onion-key\n".getBytes(Tor.getDefaultCharset());
	private final static int MAX_WARNING_COUNT = 5;
	private int warningCount = 0;
	private final ByteBuffer buffer;
	private final DocumentParserFactory parserFactory = new DocumentParserFactoryImpl();
	
	public MicrodescriptorCacheLoader(ByteBuffer buffer) {
		this.buffer = buffer;
	}

	public List<RouterMicrodescriptor> reload() {
		final List<MicrodescriptorCacheLocation> locations = createLocationList();
		final List<RouterMicrodescriptor> descriptors = new ArrayList<RouterMicrodescriptor>();
		warningCount = 0;
		for(MicrodescriptorCacheLocation loc: locations) {
			RouterMicrodescriptor md = parseDescriptorFromLocation(loc);
			if(md != null) {
				descriptors.add(md);
			}
		}
		return descriptors;
	}
	
	private List<MicrodescriptorCacheLocation> createLocationList() {
		final List<MicrodescriptorCacheLocation> locations = new ArrayList<MicrodescriptorCacheLocation>();
		for(MicrodescriptorCacheLocation loc = extractLocation(null); loc != null; loc = extractLocation(loc)) {
			locations.add(loc);
		}
		return locations;
	}

	private MicrodescriptorCacheLocation extractLocation(MicrodescriptorCacheLocation lastLocation) {
		if(lastLocation == null) {
			return extractFirstLocation();
		}
		final int next = findOffsetOfNextDescriptor();
		final int offset = lastLocation.getOffset() + lastLocation.getLength();
		if(offset == buffer.limit() || next == -1) {
			return null;
		} else {
			return new MicrodescriptorCacheLocation(offset, next - offset);
		}
	}
	
	private MicrodescriptorCacheLocation extractFirstLocation() {
		final int start = findOffsetOfNextDescriptor();
		final int next = findOffsetOfNextDescriptor();
		if(start == -1) {
			return null;
		} else if(next == -1) {
			return new MicrodescriptorCacheLocation(start, buffer.limit() - start);
		} else {
			return new MicrodescriptorCacheLocation(start, next - start);
		}
	}

	private RouterMicrodescriptor parseDescriptorFromLocation(MicrodescriptorCacheLocation location) {
		final DocumentParsingResult<RouterMicrodescriptor> result = parseFromLocation(location);
		if(result.isOkay()) {
			result.getDocument().setCacheLocation(CacheLocation.CACHED_CACHEFILE);
			return result.getDocument();
		} else if (result.isInvalid()) {
			maybeWarn("Invalid microdescriptor document parsing from cache file : "+ result.getMessage());
		} else if(result.isError()) {
			maybeWarn("Error parsing microdescriptor from cache file : "+ result.getMessage());
		}
		return null;
	}

	private void maybeWarn(String message) {
		warningCount += 1;
		if(warningCount <= MAX_WARNING_COUNT) {
			logger.warning(message);
		}
	}
	
	private DocumentParsingResult<RouterMicrodescriptor> parseFromLocation(MicrodescriptorCacheLocation location) {
		final byte[] data = extractBufferBytes(location);
		final InputStream in = new ByteArrayInputStream(data);
		final DocumentParser<RouterMicrodescriptor> parser = parserFactory.createRouterMicrodescriptorParser(in);
		return parser.parse();
	}

	private byte[] extractBufferBytes(MicrodescriptorCacheLocation location) {
		final byte[] bs = new byte[location.getLength()];
		final int savedPosition = buffer.position();
		buffer.position(location.getOffset());
		buffer.get(bs);
		buffer.position(savedPosition);
		return bs;
	}

	private int findOffsetOfNextDescriptor() {
		while(buffer.hasRemaining()) {
			int lineStart = buffer.position();
			if(currentLineLength() == FIRST_DESCRIPTOR_KEYWORD.length && testOffsetForDescriptorStart(lineStart)) {
				return lineStart;
			}
		}
		return -1;
	}

	private int currentLineLength() {
		final int lineStart = buffer.position();
		scanUntilNewline();
		return buffer.position() - lineStart;
	}

	private boolean testOffsetForDescriptorStart(int offset) {
		for(int i = 0; i < FIRST_DESCRIPTOR_KEYWORD.length; i++) {
			if(buffer.get(offset + i) != FIRST_DESCRIPTOR_KEYWORD[i]) {
				return false;
			}
		}
		return true;
	}

	private void scanUntilNewline() {
		while(buffer.hasRemaining()) {
			if(buffer.get() == '\n') {
				return;
			}
		}
	}
}
