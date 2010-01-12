package org.torproject.jtor.directory.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

import org.torproject.jtor.TorConfig;
import org.torproject.jtor.directory.Directory;
import org.torproject.jtor.directory.DirectoryStore;
import org.torproject.jtor.directory.KeyCertificate;
import org.torproject.jtor.directory.RouterDescriptor;
import org.torproject.jtor.directory.ConsensusDocument;
import org.torproject.jtor.directory.parsing.DocumentParser;
import org.torproject.jtor.directory.parsing.DocumentParserFactory;
import org.torproject.jtor.directory.parsing.DocumentParsingResultHandler;
import org.torproject.jtor.logging.LogManager;
import org.torproject.jtor.logging.Logger;

public class DirectoryStoreImpl implements DirectoryStore {
	private final Logger logger;
	private final TorConfig config;
	private final DocumentParserFactory parserFactory;


	DirectoryStoreImpl(LogManager logManager, TorConfig config) {
		this.logger = logManager.getLogger("directory-store");
		this.config = config;
		this.parserFactory = new DocumentParserFactoryImpl(logManager);
	}

	public void saveCertificates(List<KeyCertificate> certificates) {
		final File outFile = new File(config.getDataDirectory(), "certificates");
		try {
			final FileOutputStream fos = new FileOutputStream(outFile);
			final Writer writer = new OutputStreamWriter(fos, "ISO-8859-1");
			for(KeyCertificate cert: certificates) 
				writer.write(cert.getRawDocumentData());
			writer.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public void loadCertificates(final Directory directory) {
		final File inFile = new File(config.getDataDirectory(), "certificates");
		if(!inFile.exists())
			return;
		try {
			final FileInputStream fis = new FileInputStream(inFile);
			final Reader reader = new InputStreamReader(fis, "ISO-8859-1");
			final DocumentParser<KeyCertificate> parser = parserFactory.createKeyCertificateParser(reader);
			parser.parse(new DocumentParsingResultHandler<KeyCertificate>() {

				public void parsingError(String message) {
					logger.error("Parsing error loading certificates: "+ message);
				}

				public void documentParsed(KeyCertificate document) {
					directory.addCertificate(document);
				}

				public void documentInvalid(KeyCertificate document, String message) {
					logger.warning("Problem loading certificate: "+ message);
				}
			});
		} catch(IOException e) {
			e.printStackTrace();
		}

	}
	public void saveConsensus(ConsensusDocument consensus) {
		final File outFile = new File(config.getDataDirectory(), "consensus");
		try {
			final FileOutputStream fos = new FileOutputStream(outFile);
			final Writer writer = new OutputStreamWriter(fos, "ISO-8859-1");
			writer.write(consensus.getRawDocumentData());
			writer.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public void loadConsensus(final Directory directory) {
		final File inFile = new File(config.getDataDirectory(), "consensus");
		if(!inFile.exists())
			return;
		try {
			final FileInputStream fis = new FileInputStream(inFile);
			final Reader reader = new InputStreamReader(fis, "ISO-8859-1");
			final DocumentParser<ConsensusDocument> parser = parserFactory.createConsensusDocumentParser(reader);
			parser.parse(new DocumentParsingResultHandler<ConsensusDocument>() {

				public void documentInvalid(ConsensusDocument document,
						String message) {
					logger.warning("Stored consensus document is invalid: "+ message);
				}

				public void documentParsed(ConsensusDocument document) {
					directory.addConsensusDocument(document);
				}

				public void parsingError(String message) {
					logger.warning("Parsing error loading stored consensus document: "+ message);
				}
			});
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void saveRouterDescriptors(List<RouterDescriptor> descriptors) {
		final File outFile = new File(config.getDataDirectory(), "routers");
		try {
			final FileOutputStream fos = new FileOutputStream(outFile);
			final Writer writer = new OutputStreamWriter(fos, "ISO-8859-1");
			for(RouterDescriptor router: descriptors)
				writer.write(router.getRawDocumentData());
			writer.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public void loadRouterDescriptors(final Directory directory) {
		final File inFile = new File(config.getDataDirectory(), "routers");
		if(!inFile.exists())
			return;
		try {
			final FileInputStream fis = new FileInputStream(inFile);
			final Reader reader = new InputStreamReader(fis, "ISO-8859-1");
			final DocumentParser<RouterDescriptor> parser = parserFactory.createRouterDescriptorParser(reader);
			parser.parse(new DocumentParsingResultHandler<RouterDescriptor>() {

				public void documentInvalid(RouterDescriptor document,
						String message) {
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
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

}
