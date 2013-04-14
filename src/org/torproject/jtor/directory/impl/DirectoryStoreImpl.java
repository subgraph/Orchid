package org.torproject.jtor.directory.impl;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.torproject.jtor.TorConfig;
import org.torproject.jtor.directory.ConsensusDocument;
import org.torproject.jtor.directory.Directory;
import org.torproject.jtor.directory.DirectoryStore;
import org.torproject.jtor.directory.KeyCertificate;
import org.torproject.jtor.directory.RouterDescriptor;
import org.torproject.jtor.directory.parsing.DocumentParser;
import org.torproject.jtor.directory.parsing.DocumentParserFactory;
import org.torproject.jtor.directory.parsing.DocumentParsingResultHandler;

public class DirectoryStoreImpl implements DirectoryStore {
	private final static Logger logger = Logger.getLogger(DirectoryStoreImpl.class.getName());

	private final TorConfig config;
	private final DocumentParserFactory parserFactory;


	DirectoryStoreImpl(TorConfig config) {
		this.config = config;
		this.parserFactory = new DocumentParserFactoryImpl();
	}

	public void saveCertificates(List<KeyCertificate> certificates) {
		final Writer writer = openWriterFor("certificates");
		if(writer == null) {
			return;
		}
		try {
			for(KeyCertificate cert: certificates) { 
				writer.write(cert.getRawDocumentData());
			}
		} catch(IOException e) {
			logger.warning("IO Error writing certificates file: "+ e);
		} finally {
			quietClose(writer);
		}
	}

	public void loadCertificates(final Directory directory) {
		final Reader reader = openReaderFor("certificates");
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
		final Writer writer = openWriterFor("consensus");
		if(writer == null) {
			return;
		}
		try {
			writer.write(consensus.getRawDocumentData());
		} catch(IOException e) {
			logger.warning("IO error writing consensus file: "+ e);
		} finally {
			quietClose(writer);
		}
	}

	public void loadConsensus(final Directory directory) {
		final Reader reader = openReaderFor("consensus");
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
					directory.addConsensusDocument(document);
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
		final Writer writer = openWriterFor("routers");
		if(writer == null) {
			return;
		}
		try {
			for(RouterDescriptor router: descriptors) {
				writer.write(router.getRawDocumentData());
			}
		} catch(IOException e) {
			logger.warning("IO error writing to routers file");
		} finally {
			quietClose(writer);
		}
	}

	public void loadRouterDescriptors(final Directory directory) {
		final Reader reader = openReaderFor("routers");
		if(reader == null) {
			return;
		}
		try {
			final DocumentParser<RouterDescriptor> parser = parserFactory.createRouterDescriptorParser(reader);
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
		final Reader reader = openReaderFor("state");
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
		final Writer writer = openWriterFor("state"); 
		if(writer == null) {
			return;
		}
		try {
			stateFile.writeFile(writer);
		} catch (IOException e) {
			logger.warning("IO error writing to state file: "+ e);
		} finally {
			quietClose(writer);
		}
		
	}
	
	
	private Writer openWriterFor(String fileName) {
		final File file = new File(config.getDataDirectory(), fileName);
		try {
			FileOutputStream fos = new FileOutputStream(file);
			return new OutputStreamWriter(fos, getFileCharset());
		} catch (FileNotFoundException e) {
			logger.log(Level.WARNING, "Failed to open file "+ file + " for writing "+ e);
			return null;
		}
	}

	private Reader openReaderFor(String fileName) {
		final File file = new File(config.getDataDirectory(), fileName);
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
}
