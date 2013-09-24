package com.subgraph.orchid.directory;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.Charset;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.subgraph.orchid.ConsensusDocument;
import com.subgraph.orchid.ConsensusDocument.ConsensusFlavor;
import com.subgraph.orchid.Directory;
import com.subgraph.orchid.DirectoryStore;
import com.subgraph.orchid.KeyCertificate;
import com.subgraph.orchid.RouterDescriptor;
import com.subgraph.orchid.RouterMicrodescriptor;
import com.subgraph.orchid.RouterMicrodescriptor.CacheLocation;
import com.subgraph.orchid.TorConfig;
import com.subgraph.orchid.TorConfig.AutoBoolValue;
import com.subgraph.orchid.crypto.TorRandom;
import com.subgraph.orchid.directory.parsing.DocumentParser;
import com.subgraph.orchid.directory.parsing.DocumentParserFactory;
import com.subgraph.orchid.directory.parsing.DocumentParsingResultHandler;

public class DirectoryStoreImpl implements DirectoryStore {
	private final static Logger logger = Logger.getLogger(DirectoryStoreImpl.class.getName());

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
	private final DocumentParserFactory parserFactory;
	private final TorRandom random;
	private final Object microdescriptorLock;
	
	private boolean directoryCreationFailed;
	
	DirectoryStoreImpl(TorConfig config) {
		this.config = config;
		this.parserFactory = new DocumentParserFactoryImpl();
		this.random = new TorRandom();
		this.microdescriptorLock = new Object();
	}

	public void saveCertificates(List<KeyCertificate> certificates) {
		final File tempFile = createTempFile(CacheFile.CERTIFICATES);
		final Writer writer = openWriterFor(tempFile);
		if(writer == null) {
			return;
		}
		try {
			for(KeyCertificate cert: certificates) { 
				writer.write(cert.getRawDocumentData());
			}
			quietClose(writer);
			installTempFile(CacheFile.CERTIFICATES, tempFile);
		} catch(IOException e) {
			logger.warning("IO Error writing certificates file: "+ e);
		} finally {
			quietClose(writer);
		}
	}

	public void loadCertificates(final Directory directory) {
		final Reader reader = openReaderFor(CacheFile.CERTIFICATES);
		if(reader == null) {
			return;
		}
		try {
			final DocumentParser<KeyCertificate> parser = parserFactory.createKeyCertificateParser(reader);
			parser.parse(new DocumentParsingResultHandler<KeyCertificate>() {
				public void parsingError(String message) {
					logger.warning("Parsing error loading certificates: "+ message);
				}

				public void documentParsed(KeyCertificate document) {
					directory.addCertificate(document);
				}

				public void documentInvalid(KeyCertificate document, String message) {
					logger.warning("Problem loading certificate: " + message);
				}
			});
		} finally {
			quietClose(reader);
		}
	}
	
	public void saveConsensus(ConsensusDocument consensus) {
		final CacheFile cacheFile = getConsensusCacheFile(consensus.getFlavor() == ConsensusFlavor.MICRODESC); 
		final File tempFile = createTempFile(cacheFile);
		final Writer writer = openWriterFor(tempFile);
		if(writer == null) {
			return;
		}
		try {
			writer.write(consensus.getRawDocumentData());
			quietClose(writer);
			installTempFile(cacheFile, tempFile);
		} catch(IOException e) {
			logger.warning("IO error writing consensus file: "+ e);
		} finally {
			quietClose(writer);
		}
	}
	
	private CacheFile getConsensusCacheFile(boolean isMicrodescriptorFlavor) {
		if(isMicrodescriptorFlavor) {
			return CacheFile.CONSENSUS_MICRODESC;
		} else {
			return CacheFile.CONSENSUS;
		}
	}

	public void loadConsensus(final Directory directory) {
		final Reader reader = openReaderFor(getConsensusCacheFile(isUsingMicrodescriptors()));
		if(reader == null) {
			return;
		}
		try {
			final DocumentParser<ConsensusDocument> parser = parserFactory.createConsensusDocumentParser(reader);
			parser.parse(new DocumentParsingResultHandler<ConsensusDocument>() {
				public void documentInvalid(ConsensusDocument document,	String message) {
					logger.warning("Stored consensus document is invalid: "+ message);
				}
			
				public void documentParsed(ConsensusDocument document) {
					directory.addConsensusDocument(document, true);
				}

				public void parsingError(String message) {
					logger.warning("Parsing error loading stored consensus document: "+ message);
				}
			});
		} finally { 
			quietClose(reader);
		}
	}
	
	public void saveRouterDescriptors(List<RouterDescriptor> descriptors) {
		final File tempFile = createTempFile(CacheFile.DESCRIPTORS);
		final Writer writer = openWriterFor(tempFile);
		if(writer == null) {
			return;
		}
		try {
			for(RouterDescriptor router: descriptors) {
				writer.write(router.getRawDocumentData());
			}
			quietClose(writer);
			installTempFile(CacheFile.DESCRIPTORS, tempFile);
		} catch(IOException e) {
			logger.warning("IO error writing to routers file");
		} finally {
			quietClose(writer);
		}
	}

	public void loadRouterDescriptors(final Directory directory) {
		final Reader reader = openReaderFor(CacheFile.DESCRIPTORS);
		if(reader == null) {
			return;
		}
		try {
			final DocumentParser<RouterDescriptor> parser = parserFactory.createRouterDescriptorParser(reader, false);
			parser.parse(new DocumentParsingResultHandler<RouterDescriptor>() {

				public void documentInvalid(RouterDescriptor document,	String message) {
					logger.warning("Router descriptor "+ document.getNickname() +" invalid: "+ message);
					directory.markDescriptorInvalid(document);
				}

				public void documentParsed(RouterDescriptor document) {
					directory.addRouterDescriptor(document);
				}

				public void parsingError(String message) {
					logger.warning("Parsing error loading router descriptors: "+ message);
				}
			});
		} finally {
			quietClose(reader);
		}
	}

	void loadStateFile(StateFile stateFile) {
		final Reader reader = openReaderFor(CacheFile.STATE);
		if(reader == null) {
			return;
		}
		try {
			stateFile.parseFile(reader);
		} catch (IOException e) {
			logger.warning("IO error reading state file: "+ e);
		} finally {
			quietClose(reader);
		}
	}
	
	void saveStateFile(StateFile stateFile) {
		final File tempFile = createTempFile(CacheFile.STATE);
		final Writer writer = openWriterFor(tempFile); 
		if(writer == null) {
			return;
		}
		try {
			stateFile.writeFile(writer);
			quietClose(writer);
			installTempFile(CacheFile.STATE, tempFile);
		} catch (IOException e) {
			logger.warning("IO error writing to state file: "+ e);
		} finally {
			quietClose(writer);
		}
		
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

	private Writer openAppenderFor(File file) {
		return openOutputFile(file, true);
	}

	private Writer openWriterFor(File file) {
		return openOutputFile(file, false);
	}
	
	private Writer openOutputFile(File file, boolean isAppend) {
		createDirectoryIfAbsent(config.getDataDirectory());
		try {
			FileOutputStream fos = new FileOutputStream(file, isAppend);
			return new OutputStreamWriter(fos, getFileCharset());
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
	private Reader openReaderFor(CacheFile cacheFile) {
		final File file = new File(config.getDataDirectory(), cacheFile.getBaseName());
		if(!file.exists()) {
			return null;
		}
		try {
			final FileInputStream fis = new FileInputStream(file);
			return new InputStreamReader(fis, getFileCharset());
		} catch (FileNotFoundException e) {
			return null;
		}
	}
	
	private Charset getFileCharset() {
		final String csName = "ISO-8859-1";
		if(Charset.isSupported(csName)) {
			return Charset.forName(csName);
		} else {
			return Charset.defaultCharset();
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
			final File targetFile = new File(config.getDataDirectory(), "cached-microdescs.new");
			final Writer writer = openAppenderFor(targetFile);
			if(writer == null) {
				System.out.println("open appender failed");
				return;
			}
			writeMicrodescriptorsToWriter(descriptors, writer);
		}
	}

	private void writeMicrodescriptorsToWriter(List<RouterMicrodescriptor> descriptors, Writer writer) {
		try {
			for(RouterMicrodescriptor md: descriptors) {
				md.setCacheLocation(CacheLocation.CACHED_JOURNAL);
				writer.write(md.getRawDocumentData());
			}
		} catch (IOException e) {
			logger.warning("I/O error writing to microdescriptor journal: "+ e);
		} finally {
			quietClose(writer);
		}
	}
	
	private ByteBuffer mapMicrodescriptorCache() {
		final RandomAccessFile raf = openMicrodescriptorCacheFile();
		if(raf == null) {
			return ByteBuffer.allocate(0);
		}
		final FileChannel channel = raf.getChannel();
		try {
			return channel.map(MapMode.READ_ONLY, 0, channel.size());
		} catch (IOException e) {
			logger.warning("I/O error mapping microdescriptor cache "+ e);
			return ByteBuffer.allocate(0);
		} finally {
			quietClose(raf);
		}
	}
	
	private RandomAccessFile openMicrodescriptorCacheFile() {
		final File file = new File(config.getDataDirectory(), CacheFile.MICRODESCRIPTOR_CACHE.getBaseName());
		try {
			return new RandomAccessFile(file, "r");
		} catch (FileNotFoundException e) {
			return null;
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

	private ByteBuffer loadMicrodescriptorJournal() {
		final FileInputStream fis = openMicrodescriptorJournal();
		if(fis == null) {
			return ByteBuffer.allocate(0);
		}
		try {
			final FileChannel channel = fis.getChannel();
			final ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
			while(buffer.hasRemaining()) {
				if(channel.read(buffer) == -1) {
					logger.warning("Unexpected EOF reading microdescriptor journal file");
					return ByteBuffer.allocate(0);
				}
			}
			return buffer;
		} catch (IOException e) {
			logger.warning("I/O error reading microdescriptor journal file");
			return ByteBuffer.allocate(0);
		} finally {
			quietClose(fis);
		}
	}


	
	
	private FileInputStream openMicrodescriptorJournal() {
		final File journalFile = new File(config.getDataDirectory(), "cached-microdescs.new");
		if(!journalFile.exists() || journalFile.length() == 0) {
			return null;
		}
		try {
			return new FileInputStream(journalFile);
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	public ByteBuffer[] loadMicrodescriptorCache() {
		final ByteBuffer[] buffers = new ByteBuffer[2];
		synchronized (microdescriptorLock) {
			buffers[0] = mapMicrodescriptorCache();
			buffers[1] = loadMicrodescriptorJournal();
			return buffers;
		}
	}
}
