package com.subgraph.orchid.directory;

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
		final List<ByteBuffer> buffers = createBufferList();
		final List<RouterMicrodescriptor> descriptors = new ArrayList<RouterMicrodescriptor>();
		warningCount = 0;
		logger.fine("parsing descriptors from "+ buffers.size() +" buffers");
		for(ByteBuffer bb: buffers) {
			RouterMicrodescriptor md = parseDescriptorFromBuffer(bb);
			if(md != null) {
				descriptors.add(md);
			}
		}
		return descriptors;
	}

	private List<ByteBuffer> createBufferList() {
		final List<ByteBuffer> buffers = new ArrayList<ByteBuffer>();
		for(ByteBuffer bb = extractBuffer(); bb != null; bb = extractBuffer()) {
			buffers.add(bb);
		}
		return buffers;
	}

	private ByteBuffer extractBuffer() {
		if(buffer.position() == 0 && seekToFirstDescriptor() == buffer.limit()) {
			return null;
		}
		
		final int start = buffer.position();
		if(start == buffer.limit()) {
			return null;
		}
		final int length = seekToNextDescriptor() - start;
		return extractSlice(start, length);
	}
	
	private RouterMicrodescriptor parseDescriptorFromBuffer(ByteBuffer bb) {
		final DocumentParser<RouterMicrodescriptor> parser = parserFactory.createRouterMicrodescriptorParser(bb);
		final DocumentParsingResult<RouterMicrodescriptor> result = parser.parse();
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
	
	private ByteBuffer extractSlice(int position, int length) {
		final int savedPosition = buffer.position();
		final int savedLimit = buffer.limit();
		buffer.position(position);
		buffer.limit(position + length);
		final ByteBuffer slice = buffer.slice();
		buffer.limit(savedLimit);
		buffer.position(savedPosition);
		return slice;
	}
	
	private int seekToFirstDescriptor() {
		buffer.position(0);
		int off = findOffsetOfNextDescriptor();
		if(off == -1) {
			return buffer.limit();
		} else {
			buffer.position(off);
			return off;
		}
	}

	private int seekToNextDescriptor() {
		buffer.position(buffer.position() + FIRST_DESCRIPTOR_KEYWORD.length);
		int offset = findOffsetOfNextDescriptor();
		if(offset == -1) {
			return buffer.limit();
		} else {
			buffer.position(offset);
			return offset;
		}
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
