package org.torproject.jtor.directory.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.torproject.jtor.data.HexDigest;
import org.torproject.jtor.directory.Directory;
import org.torproject.jtor.directory.DirectoryServer;
import org.torproject.jtor.directory.KeyCertificate;

public class NetworkStatusManager {
	
	private final Directory directory;
	
	NetworkStatusManager(Directory directory) {
		this.directory = directory;
	}
	
	private void checkValidCertificates() {
		final List<HexDigest> neededCertificates = new ArrayList<HexDigest>(); 
		for(DirectoryServer dir: directory.getDirectoryAuthorities()) {
			final KeyCertificate certificate = directory.findCertificate(dir.getFingerprint());
			if(certificate == null || certificate.isExpired()) 
				neededCertificates.add(dir.getFingerprint());
		}
		if(!neededCertificates.isEmpty())
			requestCertificates(neededCertificates);
		
	}
	
	private void requestCertificates(final List<HexDigest> certificates) {
		Thread requestThread = new Thread(new Runnable() {

			public void run() {
				runRequestCertificates(certificates);
				// TODO Auto-generated method stub
				
			}
			
		});
		requestThread.start();
	}
	private void runRequestCertificates(List<HexDigest> certificates) {
		final DirectoryServer directoryAuthority = directory.getRandomDirectoryAuthority();
		final DirectoryConnection directoryConnection = openDirectConnectionToDirectoryServer(directoryAuthority);
		
	}
	
	private DirectoryConnection openDirectConnectionToDirectoryServer(DirectoryServer server) {
		final InetAddress address = server.getAddress().toInetAddress();
		final int port = server.getDirectoryPort();
		try {
			final Socket socket = new Socket(address, port);
			return new DirectoryConnection(socket.getInputStream(), socket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}

}
