package org.torproject.jtor;


import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.torproject.jtor.directory.Directory;
import org.torproject.jtor.directory.impl.DirectoryImpl;
import org.torproject.jtor.directory.impl.DocumentParserFactoryImpl;
import org.torproject.jtor.directory.parsing.DocumentParserFactory;

public class Tor {
	private final Directory directory;
	private final DocumentParserFactory parserFactory;
	private final Logger logger;
	
	public Tor() {
		this(new ConsoleLogger());
	}
	
	public Tor(Logger logger) {
		Security.addProvider(new BouncyCastleProvider());
		this.logger = logger;
		this.directory = new DirectoryImpl(logger);
		parserFactory = new DocumentParserFactoryImpl(logger);
	}
	
	public Logger getLogger() {
		return logger;
	}
	
	public Directory getDirectory() {
		return directory;
	}
	
	public DocumentParserFactory getDocumentParserFactory() {
		return parserFactory;
	}

}
