package com.subgraph.orchid.directory;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.subgraph.orchid.ConsensusDocument;
import com.subgraph.orchid.ConsensusDocument.ConsensusFlavor;
import com.subgraph.orchid.DirectoryStore;
import com.subgraph.orchid.Document;
import com.subgraph.orchid.KeyCertificate;
import com.subgraph.orchid.RouterDescriptor;
import com.subgraph.orchid.RouterMicrodescriptor;
import com.subgraph.orchid.RouterMicrodescriptor.CacheLocation;
import com.subgraph.orchid.Tor;
import com.subgraph.orchid.TorConfig;
import com.subgraph.orchid.TorConfig.AutoBoolValue;
import com.subgraph.orchid.crypto.TorRandom;

public class DirectoryStoreImpl implements DirectoryStore {
	private final static Logger logger = Logger.getLogger(DirectoryStoreImpl.class.getName());
	private final static ByteBuffer EMPTY_BUFFER = ByteBuffer.allocate(0);
	
	private enum FileLoadMode { MODE_BASIC, MODE_DIRECT_BUFFER, MODE_MEMORY_MAP };
	private enum CacheFile {
		CERTIFICATES("certificates"),
		CONSENSUS("consensus"),
		CONSENSUS_MICRODESC("consensus-microdesc"),
		MICRODESCRIPTOR_CACHE("cached-microdescs"),
		MICRODESCRIPTOR_JOURNAL("cached-microdescs.new"),
		DESCRIPTORS("routers"),
		STATE("state");
		
		final private String baseName;
		CacheFile(String baseName) {
			this.baseName = baseName;
		}
		
		String getBaseName() {
			return baseName;
		}
	}
	
	private final TorConfig config;
	private final TorRandom random;
	private final Object microdescriptorLock;
	
	private boolean directoryCreationFailed;
	private FileLoadMode defaultLoadMode = FileLoadMode.MODE_DIRECT_BUFFER;
	
	DirectoryStoreImpl(TorConfig config) {
		this.config = config;
		this.random = new TorRandom();
		this.microdescriptorLock = new Object();
	}

	public void saveCertificates(List<KeyCertificate> certificates) {
		writeDocuments(certificates, CacheFile.CERTIFICATES);
	}
	
	public ByteBuffer loadCertificates() {
		return loadFile(CacheFile.CERTIFICATES);
	}
	
	public void saveConsensus(ConsensusDocument consensus) {
		final CacheFile cacheFile = getConsensusCacheFile(consensus.getFlavor() == ConsensusFlavor.MICRODESC);
		writeDocuments(Arrays.asList(consensus), cacheFile);
	}
	
	private CacheFile getConsensusCacheFile(boolean isMicrodescriptorFlavor) {
		if(isMicrodescriptorFlavor) {
			return CacheFile.CONSENSUS_MICRODESC;
		} else {
			return CacheFile.CONSENSUS;
		}
	}

	public ByteBuffer loadConsensus() {
		return loadFile(getConsensusCacheFile(isUsingMicrodescriptors()));
	}
	
	public void saveRouterDescriptors(List<RouterDescriptor> descriptors) {
		writeDocuments(descriptors, CacheFile.DESCRIPTORS);
	}

	public ByteBuffer loadRouterDescriptors() {
		return loadFile(CacheFile.DESCRIPTORS);
	}

	ByteBuffer loadStateFile() {
		return loadFile(CacheFile.STATE);
	}
	
	void saveStateFile(StateFile stateFile) {
		final File f = getFileForWrite(CacheFile.STATE, false);
		final FileOutputStream fos = openOutputStream(f, false);
		if(fos == null) {
			return;
		}
		final ByteBuffer fileData = stateFile.getFileContents();
		final FileChannel channel = fos.getChannel();
		try {
			while(fileData.hasRemaining()) {
				channel.write(fileData);
			}
		} catch (IOException e) {
			logger.warning("I/O error writing to state file : "+ e);
			return;
		} finally {
			quietClose(fos);
		}
		installTempFile(CacheFile.STATE, f);
	}
	
	
	private File createTempFile(CacheFile cacheFile) {
		final long n = random.nextLong();
		final File f = new File(config.getDataDirectory(), cacheFile.getBaseName() + Long.toString(n));
		f.deleteOnExit();
		return f;
	}

	private void installTempFile(CacheFile cacheFile, File tempFile) {
		final File target = new File(config.getDataDirectory(), cacheFile.getBaseName());
		target.delete();
		if(!tempFile.renameTo(target)) {
			logger.warning("Failed to rename temp file "+ tempFile + " to "+ target);
		}
		tempFile.delete();
	}

	private Writer openWriterFor(File file) {
		final FileOutputStream fos = openOutputStream(file, false);
		return (fos == null) ? (null) : (new OutputStreamWriter(fos, Tor.getDefaultCharset()));
	}
	
	private FileOutputStream openOutputStream(File file, boolean isAppend) {
		createDirectoryIfAbsent(config.getDataDirectory());
		try {
			return new FileOutputStream(file, isAppend);
		} catch (FileNotFoundException e) {
			logger.log(Level.WARNING, "Failed to open file "+ file + " for writing "+ e);
			return null;
		}
	}

	private void createDirectoryIfAbsent(File dataDirectory) {
		if(directoryCreationFailed) {
			return;
		}
		if(!dataDirectory.exists()) {
			if(!dataDirectory.mkdirs()) {
				directoryCreationFailed = true;
				logger.warning("Failed to create data directory "+ dataDirectory);
			}
		}
	}
	
	private void quietClose(Closeable closeable) {
		try {
			closeable.close();
		} catch (IOException e) {
		}
	}
	
	private boolean isUsingMicrodescriptors() {
		return config.getUseMicrodescriptors() != AutoBoolValue.FALSE;
	}

	public void appendMicrodescriptorsToJournal(List<RouterMicrodescriptor> descriptors) {	
		synchronized(microdescriptorLock) {
			writeDocuments(descriptors, CacheFile.MICRODESCRIPTOR_JOURNAL, true);
			for(RouterMicrodescriptor md: descriptors) {
				md.setCacheLocation(CacheLocation.CACHED_JOURNAL);
			}
		}
	}
	
	public void writeMicrodescriptorCache(List<RouterMicrodescriptor> descriptors, boolean removeJournal) {
		final File tempFile = createTempFile(CacheFile.MICRODESCRIPTOR_CACHE);
		final Writer writer = openWriterFor(tempFile); 
		if(writer == null) {
			return;
		}
		try {
			for(RouterMicrodescriptor md: descriptors) {
				md.setCacheLocation(CacheLocation.CACHED_CACHEFILE);
				writer.write(md.getRawDocumentData());
			}
			quietClose(writer);
			synchronized (microdescriptorLock) {
				installTempFile(CacheFile.MICRODESCRIPTOR_CACHE, tempFile);
				if(removeJournal) {
					File journalFile = new File(config.getDataDirectory(), CacheFile.MICRODESCRIPTOR_JOURNAL.getBaseName());
					journalFile.delete();
				}
			}
			
		} catch (IOException e) {
			logger.warning("IO error writing to microdescriptor cache file: "+ e);
		} finally {
			quietClose(writer);
		}
	}

	public ByteBuffer[] loadMicrodescriptorCache() {
		final ByteBuffer[] buffers = new ByteBuffer[2];
		synchronized (microdescriptorLock) {
			buffers[0] = loadFile(CacheFile.MICRODESCRIPTOR_CACHE);
			buffers[1] = loadFile(CacheFile.MICRODESCRIPTOR_JOURNAL);
			return buffers;
		}
	}
	
	private ByteBuffer loadFile(CacheFile cacheFile) {
		return loadFile(cacheFile, defaultLoadMode);
	}

	private ByteBuffer loadFile(CacheFile cacheFile, FileLoadMode loadMode) {
		if(loadMode == FileLoadMode.MODE_MEMORY_MAP) {
			return mapFile(cacheFile);
		} else {
			return readFile(cacheFile, loadMode == FileLoadMode.MODE_DIRECT_BUFFER);
		}
	}
	
	private ByteBuffer mapFile(CacheFile cacheFile) {
		final RandomAccessFile raf = openRandomAccessFileForRead(cacheFile);
		if(raf == null) {
			return EMPTY_BUFFER;
		}
		final FileChannel channel = raf.getChannel();
		try {
			return channel.map(MapMode.READ_ONLY, 0, channel.size());
		} catch (IOException e) {
			logger.warning("I/O error memory mapping cache file "+ cacheFile.getBaseName() + " : "+ e);
			return EMPTY_BUFFER;
		} finally {
			quietClose(raf);
		}
	}
	
	private RandomAccessFile openRandomAccessFileForRead(CacheFile cacheFile) {
		final File file = new File(config.getDataDirectory(), cacheFile.getBaseName());
		try {
			return new RandomAccessFile(file, "r");
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	private ByteBuffer readFile(CacheFile cacheFile, boolean useDirectBuffer) {
		final FileInputStream fis = openFileInputStream(cacheFile);
		if(fis == null) {
			return ByteBuffer.allocate(0);
		}
		try {
			final FileChannel channel = fis.getChannel();
			final int sz = (int) (channel.size() & 0xFFFFFFFF);
			final ByteBuffer buffer = (useDirectBuffer) ? ByteBuffer.allocateDirect(sz) : ByteBuffer.allocate(sz); 
			while(buffer.hasRemaining()) {
				if(channel.read(buffer) == -1) {
					logger.warning("Unexpected EOF reading cache file "+ cacheFile.getBaseName());
					return ByteBuffer.allocate(0);
				}
			}
			buffer.rewind();
			return buffer;
		} catch (IOException e) {
			logger.warning("I/O error reading cache file "+ cacheFile.getBaseName());
			return ByteBuffer.allocate(0);
		} finally {
			quietClose(fis);
		}
	}
	
	private FileInputStream openFileInputStream(CacheFile cacheFile) {
		final File f = new File(config.getDataDirectory(), cacheFile.getBaseName());
		if(!f.exists() || f.length() == 0) {
			return null;
		}
		try {
			return new FileInputStream(f);
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	private void writeDocuments(List<? extends Document> documents, CacheFile cacheFile) {
		writeDocuments(documents, cacheFile, false);
	}

	private void writeDocuments(List<? extends Document> documents, CacheFile cacheFile, boolean isAppend) {
		final File f = getFileForWrite(cacheFile, isAppend);
		final FileOutputStream fos = openOutputStream(f, isAppend);
		if(fos == null) {
			return;
		}
	
		try {
			writeDocumentsToChannel(fos.getChannel(), documents);
		} catch (IOException e) {
			logger.warning("I/O error writing to cache file "+ cacheFile.getBaseName());
			return;
		} finally {
			quietClose(fos);
		}
		
		if(!isAppend) {
			installTempFile(cacheFile, f);
		}
	}

	private File getFileForWrite(CacheFile cacheFile, boolean isAppend) {
		if(isAppend) {
			return new File(config.getDataDirectory(), cacheFile.getBaseName());
		} else {
			return createTempFile(cacheFile);
		}
	}

	private void writeDocumentsToChannel(WritableByteChannel channel, List<? extends Document> documents) throws IOException {
		for(Document d: documents) {
			writeAllToChannel(channel, d.getRawDocumentBytes());
		}
	}

	private void writeAllToChannel(WritableByteChannel channel, ByteBuffer data) throws IOException {
		data.rewind();
		while(data.hasRemaining()) {
			channel.write(data);
		}
	}
}
