package org.torproject.jtor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.zip.InflaterInputStream;

import org.torproject.jtor.circuits.Circuit;
import org.torproject.jtor.circuits.CircuitBuildHandler;
import org.torproject.jtor.circuits.CircuitNode;
import org.torproject.jtor.circuits.Connection;
import org.torproject.jtor.directory.Directory;
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
			parser.parseAndAddToDirectory(directory);
			List<RouterDescriptor> documents = parser.getDocuments();
			System.out.println("Parser returned "+ documents.size() +" router descriptors.");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (TorParsingException e) {
			e.printStackTrace();
		}
	}
	
	void circuitTest()  {
		
		String[] path = {  "AnonymousRelay", "CodeGnomeTor1", "bambi", "TorVidalia"};
		
		Circuit c = tor.createCircuitFromNicknames(Arrays.asList(path));
		
		c.openCircuit(new CircuitBuildHandler() {
			
			public void nodeAdded(CircuitNode node) {
				System.out.println("Node added: "+ node.getRouter().getNickname());				
			}
			
			public void connectionFailed(String reason) {
				System.out.println("Connect to entry router failed: "+ reason);				
			}
			
			public void connectionCompleted(Connection connection) {
				System.out.println("Connected to entry router: "+ connection.getRouter().getNickname());				
			}
			
			public void circuitBuildFailed(String reason) {
				System.out.println("Circuit creation failed: "+ reason);				
			}

			public void circuitBuildCompleted(Circuit circuit) {
				System.out.println("Circuit creation completed successfully");
				
			}
		});
		//c.extendCircuit(tor.getDirectory().getRouterByName("bambi"));
		
		System.out.println("DONE");
	}
	
	public static void main(String[] args) {
		TorTest test = new TorTest();
		test.loadRouters();
		test.circuitTest();
	}
}
