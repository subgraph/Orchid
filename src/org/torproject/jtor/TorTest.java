package org.torproject.jtor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.InflaterInputStream;

import org.torproject.jtor.directory.Directory;
import org.torproject.jtor.directory.DirectoryServer;
import org.torproject.jtor.directory.KeyCertificate;
import org.torproject.jtor.directory.RouterDescriptor;
import org.torproject.jtor.directory.StatusDocument;
import org.torproject.jtor.directory.parsing.DocumentParser;

public class TorTest {
	final static String DATA_BASE = "/Users/bob/projects/tor/directory/";
	final static String CERTIFICATE_FILE = DATA_BASE +"certificates";
	final static String CONSENSUS_FILE = DATA_BASE +"consensus";
	final static String ROUTER_FILE = DATA_BASE +"all.z";

	private final Tor tor;
	private final Directory directory;
	TorTest() {
		tor = new Tor();
		directory = tor.getDirectory();
	}
	
	void loadCertificates() {
		try {
			final FileInputStream fis = new FileInputStream(CERTIFICATE_FILE);
			final DocumentParser<KeyCertificate> parser = tor.getDocumentParserFactory().createKeyCertificateParser(fis);
			parser.parse();
			List<KeyCertificate> documents = parser.getDocuments();
			System.out.println("Parser returned "+ documents.size() +" certificate documents.");
			for(KeyCertificate cert: documents) {
				//System.out.println("Adding: "+ cert);
				directory.addCertificate(cert);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch(TorParsingException e) {
			e.printStackTrace();
		}
	}
	
	void loadConsensus() {
		try {
			final InputStream in = new FileInputStream(CONSENSUS_FILE);
			final DocumentParser<StatusDocument> parser = tor.getDocumentParserFactory().createStatusDocumentParser(in);
			parser.parse();
			List<StatusDocument> documents = parser.getDocuments();
			System.out.println("Parser returned "+ documents.size() +" consensus documents.");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (TorParsingException e) {
			e.printStackTrace();
		}
	}
	
	void loadRouters() {
		try {
			final InputStream in = new FileInputStream(ROUTER_FILE);
			final InputStream inflater = new InflaterInputStream(in);
			DocumentParser<RouterDescriptor> parser = tor.getDocumentParserFactory().createRouterDescriptorParser(inflater);
			parser.parse();
			List<RouterDescriptor> documents = parser.getDocuments();
			System.out.println("Parser returned "+ documents.size() +" router descriptors.");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (TorParsingException e) {
			e.printStackTrace();
		}
	}
	
	void randomAuthority() {
		DirectoryServer randomAuthority = directory.getRandomDirectoryAuthority();
		System.out.println("Random authority: "+ randomAuthority);
		KeyCertificate cert = directory.findCertificate(randomAuthority.getFingerprint());
		System.out.println("Random authority certificate: " + cert);
	}
	
	void runTests() {
		loadConsensus();
		loadRouters();
		loadCertificates();
		randomAuthority();
	}
	
	public static void main(String[] args) {
		TorTest test = new TorTest();
		test.runTests();
	}
}
