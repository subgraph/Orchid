package org.torproject.jtor.socks.impl;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.torproject.jtor.Logger;
import org.torproject.jtor.circuits.Stream;

public class SocksStreamConnection {
	
	public static void runConnection(InputStream socketIn, OutputStream socketOut, Stream stream, Logger logger) {
		SocksStreamConnection ssc = new SocksStreamConnection(socketIn, socketOut, stream, logger);
		ssc.run();
	}
	private final static int TRANSFER_BUFFER_SIZE = 1024;
	private final InputStream torInputStream;
	private final OutputStream torOutputStream;
	private final InputStream socketInputStream;
	private final OutputStream socketOutputStream;
	private final Logger logger;
	private final Thread incomingThread;
	private final Thread outgoingThread;
	private final Object lock = new Object();
	private volatile boolean outgoingClosed;
	private volatile boolean incomingClosed;
	
	
	private SocksStreamConnection(InputStream socketIn, OutputStream socketOut, Stream stream, Logger logger) {
		torInputStream = stream.getInputStream();
		torOutputStream = stream.getOutputStream();
		socketInputStream = socketIn;
		socketOutputStream = socketOut;
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
			closeStream(torInputStream);
			closeStream(torOutputStream);
			closeStream(socketInputStream);
			closeStream(socketOutputStream);
		}
	}
	
	private Thread createIncomingThread() {
		return new Thread(new Runnable() { public void run() {
			try {
				incomingTransferLoop();
			} catch (IOException e) {
				logger.warn("System error on incoming stream IO : "+ e.getMessage());
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
				e.printStackTrace();
				logger.warn("System error on outgoing stream IO : "+ e.getMessage());
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
				logger.debug("EOF on TOR input stream "+ torInputStream);
				return;
			} else if(n > 0) {
				logger.debug("Transferring "+ n +" bytes from "+ torInputStream +" to SOCKS socket");
				socketOutputStream.write(incomingBuffer, 0, n);
				socketOutputStream.flush();
			}
		}
	}
	private void outgoingTransferLoop() throws IOException {
		final byte[] outgoingBuffer = new byte[TRANSFER_BUFFER_SIZE];
		while(true) {
			final int n = socketInputStream.read(outgoingBuffer);
			if(n == -1) {
				logger.debug("EOF on SOCKS socket connected to "+ torOutputStream);
				return;
			} else if(n > 0) {
				logger.debug("Transferring "+ n +" bytes from SOCKS socket to "+ torOutputStream);
				torOutputStream.write(outgoingBuffer, 0, n);
				torOutputStream.flush();
			}
		}
	}
	
	private void closeStream(Closeable c) {
		try {
			c.close();
		} catch (IOException e) {
			logger.warn("Close failed on "+ c + " : "+ e.getMessage());
		}	
	}
}
