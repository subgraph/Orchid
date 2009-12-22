package org.torproject.jtor.socks.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import org.torproject.jtor.Logger;
import org.torproject.jtor.TorException;
import org.torproject.jtor.circuits.Stream;
import org.torproject.jtor.circuits.impl.StreamManagerImpl;
import org.torproject.jtor.data.IPv4Address;

public class SocksClientSocket {
	private final static int SOCKS_COMMAND_CONNECT = 1;
	static void runClient(InputStream in, OutputStream out, Logger logger, StreamManagerImpl streamManager) throws IOException {
		final SocksClientSocket client = new SocksClientSocket(in, out, logger, streamManager);
		client.run();
	}
	private final InputStream in;
	private final OutputStream out;
	private final Logger logger;
	private final StreamManagerImpl streamManager;
	private SocksClientSocket(InputStream in, OutputStream out, Logger logger, StreamManagerImpl streamManager) {
		this.in = in;
		this.out = out;
		this.logger = logger;
		this.streamManager = streamManager;
	}
	
	private void run() throws IOException {
		final int version = in.read();
		switch(version) {
		case 'H':
		case 'G':
		case 'P':
			sendHttpPage();
			break;
		case 4:
			processVersion4Command();
			break;
		case 5:
			processVersion5Command();
			break;
		default:
			// XXX
		}
	}

	private void processVersion4Command() throws IOException {
		final int command = in.read();
		if(command != SOCKS_COMMAND_CONNECT) {
			sendVersion4Error();
			return;
		}
		byte[] addressBuffer = new byte[6];
		if(!readAll(addressBuffer, 0, 6))
			return;
		
		ByteBuffer bb = ByteBuffer.wrap(addressBuffer);
		int port = bb.getShort() & 0xFFFF;
		int addr = bb.getInt();
		try {
			final Stream stream = openVersion4ConnectStream(addr, port);
			sendVersion4Success();
			SocksStreamConnection.runConnection(in, out, stream, logger);
		} catch(InterruptedException e) {
			sendVersion4Error();
			Thread.currentThread().interrupt();
			return;
		}
	}
	
	private Stream openVersion4ConnectStream(int address, int port) throws IOException, InterruptedException {
		readNullTerminatedString(); // username
		if(isVersion4aHostname(address)) {
			final String hostname = readNullTerminatedString();
			logger.debug("CONNECT command received for "+ hostname +" : "+ port);
			return streamManager.openExitStreamTo(hostname, port);
		}  else {
			final IPv4Address a = new IPv4Address(address);
			logger.debug("CONNECT command received for address "+ a +":"+ port);
			return streamManager.openExitStreamTo(a, port);
		}
		
	}
	private boolean isVersion4aHostname(int n) {
		/*
		 * For version 4A, if the client cannot resolve the destination host's
		 * domain name to find its IP address, it should set the first three bytes
         * of DSTIP to NULL and the last byte to a non-zero value. (This corresponds
         * to IP address 0.0.0.x, with x nonzero.
		 */
		return ((n & 0xFFFFFF00) == 0) && ((n & 0xFF) != 0);
	}
	
	private String readNullTerminatedString() throws IOException {
		final StringBuilder sb = new StringBuilder();
		while(true) {
			final int c = in.read();
			if(c == -1)
				return null;
			if(c == 0)
				return sb.toString();
			char ch = (char) c;
			sb.append(ch);
		}
	}
	
	private void sendVersion4Error() throws IOException {
		byte[] errorBuffer = new byte[8];
		errorBuffer[0] = 0;
		errorBuffer[1] = 91;
		out.write(errorBuffer);
		out.close();
	}
	
	private void sendVersion4Success() throws IOException {
		byte[] responseBuffer = new byte[8];
		responseBuffer[0] = 0;
		responseBuffer[1] = 90;
		out.write(responseBuffer);
	}

	private void processVersion5Command() {
		throw new TorException("SOCKS v5 not implemented yet");
	}

	private void sendHttpPage() {
		throw new TorException("Returning HTTP page not implemented");
	}

	private boolean readAll(byte[] buffer, int offset, int length) throws IOException {
		while(length > 0) {
			int n = in.read(buffer, offset, length);
			if(n == -1)
				return false;
			offset += n;
			length -= n;
		}
		return true;
	}
}
