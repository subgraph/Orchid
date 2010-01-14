package org.torproject.jtor.socks.impl;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.torproject.jtor.circuits.Stream;
import org.torproject.jtor.logging.Logger;

public class SocksStreamConnection {
	
	public static void runConnection(Socket socket, Stream stream, Logger logger) {
		SocksStreamConnection ssc = new SocksStreamConnection(socket, stream, logger);
		ssc.run();
	}
	private final static int TRANSFER_BUFFER_SIZE = 4096;
	private final Stream stream;
	private final InputStream torInputStream;
	private final OutputStream torOutputStream;
	private final Socket socket;
	private final Logger logger;
	private final Thread incomingThread;
	private final Thread outgoingThread;
	private final Object lock = new Object();
	private volatile boolean outgoingClosed;
	private volatile boolean incomingClosed;

	private SocksStreamConnection(Socket socket, Stream stream, Logger logger) {
		this.socket = socket;
		this.stream = stream;
		torInputStream = stream.getInputStream();
		torOutputStream = stream.getOutputStream();
		
		this.logger = logger;
		incomingThread = createIncomingThread();
		outgoingThread = createOutgoingThread();
	}

	private void run() {
		incomingThread.start();
		outgoingThread.start();
		synchronized(lock) {
			while(!(outgoingClosed && incomingClosed)) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return;
				}
			}
			
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			closeStream(torInputStream);
			closeStream(torOutputStream);
			
		}
	}

	private Thread createIncomingThread() {
		return new Thread(new Runnable() { public void run() {
			try {
				incomingTransferLoop();
			} catch (IOException e) {
				logger.debug("System error on incoming stream IO  "+ stream +" : "+ e.getMessage());
			} finally {
				synchronized(lock) {
					incomingClosed = true;
					lock.notifyAll();
				}
			}
		}});
	}

	private Thread createOutgoingThread() {
		return new Thread(new Runnable() { public void run() {
			try {
				outgoingTransferLoop();
			} catch (IOException e) {
				logger.debug("System error on outgoing stream IO "+ stream +" : "+ e.getMessage());
			} finally {
				synchronized(lock) {
					outgoingClosed = true;
					lock.notifyAll();
				}
			}
		}});
	}

	private void incomingTransferLoop() throws IOException {
		final byte[] incomingBuffer = new byte[TRANSFER_BUFFER_SIZE];
		while(true) {
			final int n = torInputStream.read(incomingBuffer);
			if(n == -1) {
				logger.debug("EOF on TOR input stream "+ stream);
				socket.shutdownOutput();
				return;
			} else if(n > 0) {
				logger.debug("Transferring "+ n +" bytes from "+ stream +" to SOCKS socket");
				if(!socket.isOutputShutdown()) {
					socket.getOutputStream().write(incomingBuffer, 0, n);
					socket.getOutputStream().flush();
				} else {
					closeStream(torInputStream);
					return;
				}
			}
		}
	}

	private void outgoingTransferLoop() throws IOException {
		final byte[] outgoingBuffer = new byte[TRANSFER_BUFFER_SIZE];
		while(true) {
			stream.waitForSendWindow();
			final int n = socket.getInputStream().read(outgoingBuffer);
			if(n == -1) {
				logger.debug("EOF on SOCKS socket connected to "+ stream);
				return;
			} else if(n > 0) {
				logger.debug("Transferring "+ n +" bytes from SOCKS socket to "+ stream);
				torOutputStream.write(outgoingBuffer, 0, n);
				torOutputStream.flush();
			}
		}
	}

	private void closeStream(Closeable c) {
		try {
			c.close();
		} catch (IOException e) {
			logger.warning("Close failed on "+ c + " : "+ e.getMessage());
		}	
	}
}
