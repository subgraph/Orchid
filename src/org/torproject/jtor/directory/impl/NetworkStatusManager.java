package org.torproject.jtor.directory.impl;

import java.io.IOException;
import java.io.Reader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.torproject.jtor.Logger;
import org.torproject.jtor.TorException;
import org.torproject.jtor.data.HexDigest;
import org.torproject.jtor.data.IPv4Address;
import org.torproject.jtor.directory.Directory;
import org.torproject.jtor.directory.DirectoryServer;
import org.torproject.jtor.directory.KeyCertificate;
import org.torproject.jtor.directory.Router;
import org.torproject.jtor.directory.RouterDescriptor;
import org.torproject.jtor.directory.StatusDocument;
import org.torproject.jtor.directory.parsing.DocumentParser;
import org.torproject.jtor.directory.parsing.DocumentParserFactory;
import org.torproject.jtor.directory.parsing.DocumentParsingResultHandler;

public class NetworkStatusManager {
	private final static int MAX_DIRECTORY_CONNECT_ATTEMPTS = 5;
	private final static int MAX_DL_PER_REQUEST = 96;
	private final static int MAX_DL_TO_DELAY = 16;
	private final static int MIN_DL_REQUESTS = 3;
	private final static int MAX_CLIENT_INTERVAL_WITHOUT_REQUEST = 10 * 60 * 1000;
	
	private final Directory directory;
	private final Logger logger;
	private final DocumentParserFactory parserFactory;
	private Date lastDescriptorDownload;
	
	public NetworkStatusManager(Directory directory, Logger logger) {
		this.directory = directory;
		this.logger = logger;
		parserFactory = new DocumentParserFactoryImpl(logger);
	}
	
	public void startDownloadingDocuments() {
		final Thread thread = new Thread(new Runnable() {
			public void run() {
				documentDownloadLoop();	
			}
		});
		thread.start();
	}
	
	private void documentDownloadLoop() {
		logger.debug("Starting download loop");
		while(true) {
			checkValidCertificates();
			checkConsensus();
			checkDescriptors();
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
		}
	}
	
	private void checkConsensus() {
		final StatusDocument consensus = directory.getCurrentConsensusDocument();
		if(consensus != null && consensus.isLive()) {
			logger.debug("Have live consensus");
			return;
		}
		requestConsensusDocument();
	}
	
	private void requestConsensusDocument() {
		final Thread requestThread = new Thread(new Runnable() {
			public void run() {
				try {
					runRequestConsensus();
				} catch (TorException e) {
					logger.warn("Error downloading consensus document: "+ e.getMessage());				
				}
			}
		});
		requestThread.start();
	}
	
	private void runRequestConsensus() {
		final DirectoryConnection directoryConnection = openDirectConnectionToDirectoryServer();
		final Reader reader = directoryConnection.getConsensus();
		DocumentParser<StatusDocument> statusParser = parserFactory.createStatusDocumentParser(reader);
		final boolean success = statusParser.parse(new DocumentParsingResultHandler<StatusDocument>() {
			
			public void parsingError(String message) {
				logger.warn("Parsing error processing consensus document: "+ message);				
			}
			
			public void documentParsed(StatusDocument document) {
				directory.addConsensusDocument(document);				
			}
			
			public void documentInvalid(StatusDocument document, String message) {
				logger.warn("Received consensus document is invalid: "+ message);				
			}
		});
		
		if(success)
			directory.storeConsensus();
	}
	
	private void checkValidCertificates() {
		final List<HexDigest> neededCertificates = new ArrayList<HexDigest>();
		for(DirectoryServer dir: directory.getDirectoryAuthorities()) {
			if(!dir.isV3Authority())
				continue;
			final KeyCertificate certificate = directory.findCertificate(dir.getV3Identity());
			if(certificate == null || certificate.isExpired()) 
				neededCertificates.add(dir.getV3Identity());
		}
		if(!neededCertificates.isEmpty())
			requestCertificates(neededCertificates);
		else 
			logger.debug("No certificates needed");
	}
	
	private void requestCertificates(final List<HexDigest> certificates) {
		Thread requestThread = new Thread(new Runnable() {

			public void run() {
				try {
					runRequestCertificates(certificates);
				} catch (TorException e) {
					System.out.println("ERROR: "+ e.getMessage());
				}
			}
		});
		requestThread.start();
	}
	
	private void runRequestCertificates(List<HexDigest> certificates) {
		final DirectoryConnection directoryConnection = openDirectConnectionToDirectoryServer();
		final Reader reader = directoryConnection.getCertificatesByFingerprint(certificates);
		DocumentParser<KeyCertificate> certificateParser = parserFactory.createKeyCertificateParser(reader);
		final boolean success = certificateParser.parse(new DocumentParsingResultHandler<KeyCertificate>() {

			public void documentInvalid(KeyCertificate document, String message) {
				logger.warn("Received invalid certificate document: "+ message);				
			}

			public void documentParsed(KeyCertificate document) {
				directory.addCertificate(document);
			}

			public void parsingError(String message) {
				logger.warn("Parsing error processing certificate document: "+ message);				
			}
		});
		
		if(success)
			directory.storeCertificates();
	}
	
	
	private void checkDescriptors() {
		final StatusDocument consensus = directory.getCurrentConsensusDocument();
		if(consensus == null || !consensus.isLive())
			return;
		final List<Router> downloadables = directory.getRoutersWithDownloadableDescriptors();
		if(downloadables.isEmpty()) {
			logger.debug("No descriptors to download");
			return;
		}
		if(!canDownloadDescriptors(downloadables.size())) 
			return;
		
		logger.debug("Downloading: "+ downloadables.size() +" descriptors");
		for(List<Router> set: partitionDescriptors(downloadables))			
			requestDescriptors(set);
	}
	
	private boolean canDownloadDescriptors(int downloadableCount) {
		if(downloadableCount >= MAX_DL_TO_DELAY)
			return true;
		if(downloadableCount == 0)
			return false;
		if(lastDescriptorDownload == null)
			return true;
		final Date now = new Date();
		final long diff = now.getTime() - lastDescriptorDownload.getTime();
		return diff > MAX_CLIENT_INTERVAL_WITHOUT_REQUEST;	
	}
	
	/*
	 * dir-spec.txt section 5.3
	 */
	private List< List<Router> > partitionDescriptors(List<Router> descriptors) {
		final int size = descriptors.size();
		final List< List<Router> > partitions = new ArrayList< List<Router> >();
		if(size <= 10) {
			partitions.add(createPartitionList(descriptors, 0, size));
			return partitions;
		} else if(size <= (MIN_DL_REQUESTS * MAX_DL_PER_REQUEST)) {
			final int chunk = size / MIN_DL_REQUESTS;
			int over = size % MIN_DL_REQUESTS;
			int off = 0;
			for(int i = 0; i < MIN_DL_REQUESTS; i++) {
				int sz = chunk;
				if(over != 0) {
					sz++;
					over--;
				}
				partitions.add(createPartitionList(descriptors, off, sz));
				off += sz;
			}
			return partitions;
			
		} else {
			int off = 0;
			while(off < descriptors.size()) {
				partitions.add(createPartitionList(descriptors, off, MAX_DL_PER_REQUEST));
				off += MAX_DL_PER_REQUEST;
			}
			return partitions;	
		}
	}
	
	private List<Router> createPartitionList(List<Router> descriptors, int offset, int size) {
		final List<Router> newList = new ArrayList<Router>();
		for(int i = offset; i < (offset + size) && i < descriptors.size(); i++)
			newList.add(descriptors.get(i));
		return newList;
	}
	
	private void requestDescriptors(final List<Router> descriptors) {
		final Thread requestThread = new Thread(new Runnable() {
			public void run() {
				try {
					runRequestDescriptors(descriptors);
				} catch(TorException e) {
					e.printStackTrace();
				}				
			}
			
		});
		requestThread.start();
	}
	
	private void runRequestDescriptors(List<Router> descriptors) {
		final DirectoryConnection directoryConnection = openDirectConnectionToDirectoryServer();
		final List<HexDigest> digestList = new ArrayList<HexDigest>();
		final Set<HexDigest> digestSet = new HashSet<HexDigest>();
		for(Router r : descriptors) {
			digestList.add(r.getDescriptorDigest());
			digestSet.add(r.getDescriptorDigest());
		}
		final Reader reader = directoryConnection.getDescriptorsByDigests(digestList);
		DocumentParser<RouterDescriptor> parser = parserFactory.createRouterDescriptorParser(reader);
		final boolean success = parser.parse(new DocumentParsingResultHandler<RouterDescriptor>() {

			public void documentInvalid(RouterDescriptor document,
					String message) {
				logger.warn("Router descriptor "+ document.getNickname() +" invalid: "+ message);
				directory.markDescriptorInvalid(document);				
			}

			public void documentParsed(RouterDescriptor document) {
				if(!digestSet.contains(document.getDescriptorDigest())) {
					logger.warn("Server returned a router descriptor that was not requested");
					return;
				}
				directory.addRouterDescriptor(document);				
			}

			public void parsingError(String message) {
				logger.warn("Parsing error processing router descriptors: "+ message);				
			}
		});
		
		if(success)
			directory.storeDescriptors();
	}
	
	private DirectoryConnection openDirectConnectionToDirectoryServer() {
		for(int i = 0; i < MAX_DIRECTORY_CONNECT_ATTEMPTS; i++) {
			final DirectoryServer directoryAuthority = directory.getRandomDirectoryAuthority();
			try {
				return openDirectConnectionToDirectoryServer(directoryAuthority);
			} catch (IOException e) {
				logger.warn("Failed to connect to: "+ directoryAuthority +": "+ e.getMessage());
			}
		}
		throw new TorException("Giving up on direct connection to directory server");
		
	}
	private DirectoryConnection openDirectConnectionToDirectoryServer(DirectoryServer server) throws IOException {
		final InetAddress address = server.getAddress().toInetAddress();
		final int port = server.getDirectoryPort();
		final String hostString = getHostString(server.getAddress(), port);
		
		final Socket socket = new Socket(address, port);
		return new DirectoryConnection(hostString, socket.getInputStream(), socket.getOutputStream());
		
	}
	
	private String getHostString(IPv4Address address, int port) {
		if(port == 80)
			return address.toString();
		else
			return address.toString() +":"+ port;
		
	}

}
