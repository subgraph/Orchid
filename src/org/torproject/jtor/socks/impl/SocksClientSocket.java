package org.torproject.jtor.socks.impl;

import java.io.IOException;
import java.net.Socket;

import org.torproject.jtor.Logger;
import org.torproject.jtor.TorException;
import org.torproject.jtor.circuits.Stream;
import org.torproject.jtor.circuits.StreamManager;
import org.torproject.jtor.circuits.impl.StreamManagerImpl;

public class SocksClientSocket {

	static void runClient(Socket socket, Logger logger, StreamManagerImpl streamManager) throws IOException {
		final SocksClientSocket client = new SocksClientSocket(socket, logger, streamManager);
		client.run();
	}

	private final Socket socket;
	private final Logger logger;
	private final StreamManager streamManager;

	private SocksClientSocket(Socket socket, Logger logger, StreamManagerImpl streamManager) {
		this.socket = socket;
		this.logger = logger;
		this.streamManager = streamManager;
	}

	private void run() throws IOException {
		final int version = socket.getInputStream().read();
		switch(version) {
		case 'H':
		case 'G':
		case 'P':
			sendHttpPage();
			break;
		case 4:
			processRequest(new Socks4Request(socket));
			break;
		case 5:
			processRequest(new Socks5Request(socket));
			break;
		default:
			// XXX
		}
	}

	private void processRequest(SocksRequest request) {
		try {
			request.readRequest();
			if(!request.isConnectRequest()) {
				logger.warn("Non connect command");
				request.sendError();
				return;
			}
			final Stream stream = openConnectStream(request);
			request.sendSuccess();
			SocksStreamConnection.runConnection(socket, stream, logger);
		} catch (SocksRequestException e) {
			logger.warn("Failure reading SOCKS request");
		} catch (InterruptedException e) {
			logger.warn("Stream open interrupted");
			Thread.currentThread().interrupt();
		} catch (IOException e) {
			logger.warn("Error sending SOCKS response: "+ e);
		}
		
	}

	private Stream openConnectStream(SocksRequest request) throws InterruptedException {
		if(request.hasHostname()) {
			logger.debug("SOCKS CONNECT request to "+ request.getHostname() +":"+ request.getPort());
			return streamManager.openExitStreamTo(request.getHostname(), request.getPort());
		} else {
			logger.debug("SOCKS CONNECT request to "+ request.getAddress() +":"+ request.getPort());
			return streamManager.openExitStreamTo(request.getAddress(), request.getPort());
		}
	}
	private void sendHttpPage() {
		throw new TorException("Returning HTTP page not implemented");
	}
}
