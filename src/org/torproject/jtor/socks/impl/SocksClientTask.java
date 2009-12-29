package org.torproject.jtor.socks.impl;

import java.io.IOException;
import java.net.Socket;

import org.torproject.jtor.TorException;
import org.torproject.jtor.circuits.CircuitManager;
import org.torproject.jtor.circuits.OpenStreamResponse;
import org.torproject.jtor.circuits.Stream;
import org.torproject.jtor.logging.Logger;

public class SocksClientTask implements Runnable {

	private final Socket socket;
	private final Logger logger;
	private final CircuitManager circuitManager;

	SocksClientTask(Socket socket, Logger logger, CircuitManager circuitManager) {
		this.socket = socket;
		this.logger = logger;
		this.circuitManager = circuitManager;
	}

	public void run() {
		final int version = readByte();
		dispatchRequest(version);
		closeSocket();
	}

	private int readByte() {
		try {
			return socket.getInputStream().read();
		} catch (IOException e) {
			logger.warning("IO error reading version byte: "+ e.getMessage());
			return -1;
		}
	}
	
	private void dispatchRequest(int versionByte) {
		switch(versionByte) {
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
			// fall through, do nothing
		}	
	}
	
	private void processRequest(SocksRequest request) {
		try {
			request.readRequest();
			if(!request.isConnectRequest()) {
				logger.warning("Non connect command");
				request.sendError();
				return;
			}
			final OpenStreamResponse openResponse = openConnectStream(request);
			switch(openResponse.getStatus()) {
			case STATUS_STREAM_OPENED:
				request.sendSuccess();
				runOpenConnection(openResponse.getStream());
				break;
			case STATUS_ERROR_CONNECTION_REFUSED:
				request.sendConnectionRefused();
				break;
			default:
				request.sendError();
				break;
			}
			
		} catch (SocksRequestException e) {
			logger.warning("Failure reading SOCKS request");
		} catch (InterruptedException e) {
			logger.warning("Stream open interrupted");
			Thread.currentThread().interrupt();
		} catch (IOException e) {
			logger.warning("Error sending SOCKS response: "+ e);
		}
		
	}
		
	private void runOpenConnection(Stream stream) {
		SocksStreamConnection.runConnection(socket, stream, logger);
	}

	private OpenStreamResponse openConnectStream(SocksRequest request) throws InterruptedException {
		if(request.hasHostname()) {
			logger.debug("SOCKS CONNECT request to "+ request.getHostname() +":"+ request.getPort());
			return circuitManager.openExitStreamTo(request.getHostname(), request.getPort());
		} else {
			logger.debug("SOCKS CONNECT request to "+ request.getAddress() +":"+ request.getPort());
			return circuitManager.openExitStreamTo(request.getAddress(), request.getPort());
		}
	}

	private void sendHttpPage() {
		throw new TorException("Returning HTTP page not implemented");
	}

	private void closeSocket() {
		try {
			socket.close();
		} catch (IOException e) {
			logger.warning("Error closing SOCKS socket: "+ e.getMessage());
		}
	}
}
