package com.subgraph.orchid.directory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.subgraph.orchid.DirectoryStore;
import com.subgraph.orchid.RouterMicrodescriptor;
import com.subgraph.orchid.RouterMicrodescriptor.CacheLocation;
import com.subgraph.orchid.data.HexDigest;
import com.subgraph.orchid.directory.parsing.DocumentParser;
import com.subgraph.orchid.directory.parsing.DocumentParserFactory;
import com.subgraph.orchid.directory.parsing.DocumentParsingResult;
import com.subgraph.orchid.misc.GuardedBy;

public class MicrodescriptorCache {
	private final static Logger logger = Logger.getLogger(MicrodescriptorCache.class.getName());
	
	private final MicrodescriptorCacheData data;

	private final DirectoryStore store;
	private final DocumentParserFactory factory = new DocumentParserFactoryImpl();
	private final ScheduledExecutorService rebuildExecutor = Executors.newScheduledThreadPool(1);
	
	@GuardedBy("this")
	private int droppedBytes;
	
	@GuardedBy("this")
	private int journalLength;
	
	@GuardedBy("this")
	private int cacheLength;
	
	@GuardedBy("this")
	private boolean initiallyLoaded;

	MicrodescriptorCache(DirectoryStore store) {
		this.data = new MicrodescriptorCacheData();
		this.store = store;
		startRebuildTask();
	}

	
	public synchronized void initialLoad() {
		if(initiallyLoaded) {
			return;
		}
		reloadCache();
	}

	public RouterMicrodescriptor getDescriptor(HexDigest digest) {
		return data.findByDigest(digest);
	}


	public void addMicrodescriptors(List<RouterMicrodescriptor> mds) {
		final List<RouterMicrodescriptor> journalDescriptors = new ArrayList<RouterMicrodescriptor>();
		for(RouterMicrodescriptor md: mds) {
			if(data.addDescriptor(md)) {
				if(md.getCacheLocation() == CacheLocation.NOT_CACHED) {
					journalDescriptors.add(md);
				}
			}
		}

		if(!journalDescriptors.isEmpty()) {
			store.appendMicrodescriptorsToJournal(journalDescriptors);
		}
	}

	public void addMicrodescriptor(RouterMicrodescriptor md) {
		final List<RouterMicrodescriptor> mds = Arrays.asList(new RouterMicrodescriptor[] { md });
		addMicrodescriptors(mds);
	}
	
	private synchronized void clearMemoryCache() {
		data.clear();
		journalLength = 0;
		cacheLength = 0;
		droppedBytes = 0;
	}

	private synchronized void reloadCache() {
		clearMemoryCache();
		final ByteBuffer[] buffers = store.loadMicrodescriptorCache();
		loadCacheFileBuffer(buffers[0]);
		loadJournalFileBuffer(buffers[1]);
		if(!initiallyLoaded) {
			initiallyLoaded = true;
		}
	}

	
	private void loadCacheFileBuffer(ByteBuffer buffer) {
		cacheLength = buffer.limit();
		if(cacheLength == 0) {
			return;
		}
		final MicrodescriptorCacheLoader loader = new MicrodescriptorCacheLoader(buffer);
		for(RouterMicrodescriptor md: loader.reload()) {
			data.addDescriptor(md);
		}
	}
	
	private void loadJournalFileBuffer(ByteBuffer buffer) {
		journalLength = buffer.limit();
		if(journalLength == 0) {
			return;
		}
		final DocumentParsingResult<RouterMicrodescriptor> result = parseByteBuffer(buffer);
		if(result.isOkay()) {
			for(RouterMicrodescriptor md: result.getParsedDocuments()) {
				md.setCacheLocation(CacheLocation.CACHED_JOURNAL);
				data.addDescriptor(md);
			}
		} else if(result.isInvalid()) {
			logger.warning("Invalid microdescriptor data parsing from journal file : "+ result.getMessage());
		} else if(result.isError()) {
			logger.warning("Error parsing microdescriptors from journal file : "+ result.getMessage());			
		}
	}
	
	private DocumentParsingResult<RouterMicrodescriptor> parseByteBuffer(ByteBuffer buffer) {
		final byte[] bs = getBufferBytes(buffer);
		final InputStream in = new ByteArrayInputStream(bs);
		final DocumentParser<RouterMicrodescriptor> parser = factory.createRouterMicrodescriptorParser(in);
		return parser.parse();
	}
	
	private byte[] getBufferBytes(ByteBuffer buffer) {
		if(buffer.hasArray()) {
			return buffer.array();
		} else {
			final byte[] out = new byte[buffer.limit()];
			buffer.rewind();
			buffer.get(out);
			return out;
		}
	}
	
	
	private ScheduledFuture<?> startRebuildTask() {
		return rebuildExecutor.scheduleAtFixedRate(new Runnable() {
			public void run() {
				maybeRebuildCache();
			}
		}, 5, 30, TimeUnit.MINUTES);
	}
	
	private synchronized void maybeRebuildCache() {
		if(!initiallyLoaded) {
			return;
		}
		
		droppedBytes += data.cleanExpired();
		
		if(!shouldRebuildCache()) {
			return;
		}
		rebuildCache();
	}
	
	private boolean shouldRebuildCache() {
		if(journalLength < 16384) {
			return false;
		}
		if(droppedBytes > (journalLength + cacheLength) / 3) {
			return true;
		}
		if(journalLength > (cacheLength / 2)) {
			return true;
		}
		return false;
	}
	
	private void rebuildCache() {
		store.writeMicrodescriptorCache(data.getAllDescriptors(), true);
		reloadCache();
	}
}
