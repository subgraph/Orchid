package org.torproject.jtor.circuits.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.torproject.jtor.TorException;
import org.torproject.jtor.circuits.ConnectionClosedException;
import org.torproject.jtor.circuits.ConnectionConnectException;
import org.torproject.jtor.circuits.cells.Cell;
import org.torproject.jtor.data.IPv4Address;

/**
 * This class performs a Version 2 handshake as described in section 2 of
 * tor-spec.txt.  The handshake is considered complete after VERSIONS and
 * NETINFO cells have been exchanged between the two sides.
 */
public class ConnectionHandshakeV2 {
	private final static int[] SUPPORTED_CONNECTION_VERSIONS = {1,2};

	private final ConnectionImpl connection;
	private final SSLSocket socket;
	private final Object lock = new Object();
	private boolean hasRenegotiated = false;
	private boolean isFinishedHandshake = false;

	private final List<Integer> remoteVersions;
	private int remoteTimestamp;
	private IPv4Address myAddress;
	private final List<IPv4Address> remoteAddresses;

	ConnectionHandshakeV2(ConnectionImpl connection, SSLSocket socket) {
		this.connection = connection;
		this.socket = socket;
		this.socket.addHandshakeCompletedListener(new HandshakeCompletedListener() {

			public void handshakeCompleted(HandshakeCompletedEvent event) {
				processHandshakeCompleted(event);	
			}
		});
		
		this.remoteVersions = new ArrayList<Integer>();
		this.remoteAddresses = new ArrayList<IPv4Address>();
	}

	void runHandshake() throws IOException, InterruptedException {
		socket.startHandshake();
		waitForHandshakeFinished();
		sendVersions();
		receiveVersions();
		sendNetinfo();
		recvNetinfo();
	}

	int getRemoteTimestamp() {
		return remoteTimestamp;
	}

	IPv4Address getMyAddress() {
		return myAddress;
	}

	private void signalFinished() {
		synchronized (lock) {
			isFinishedHandshake = true;
			lock.notifyAll();
		}
	}

	private void processHandshakeCompleted(HandshakeCompletedEvent event) {
		if(hasRenegotiated) {
			signalFinished();
			return;
		}

		SSLSession session = socket.getSession();
		session.invalidate();
		hasRenegotiated = true;
		try {
			socket.startHandshake();
			socket.getInputStream().read(new byte[0]);
		} catch (IOException e) {
			throw new TorException(e);
		}

	}

	private void waitForHandshakeFinished() throws InterruptedException {
		synchronized(lock) {
			while(!isFinishedHandshake) 
					lock.wait();		
		}
	}

	private  void sendVersions() throws IOException {
		final Cell cell = CellImpl.createVarCell(0, Cell.VERSIONS, SUPPORTED_CONNECTION_VERSIONS.length * 2);
		for(int v: SUPPORTED_CONNECTION_VERSIONS)
			cell.putShort(v);

		connection.sendCell(cell);

	}

	private void receiveVersions() throws IOException {
		try {
			Cell c  = connection.readConnectionControlCell();
			while(c.cellBytesRemaining() >= 2)
				remoteVersions.add(c.getShort());
		} catch (ConnectionClosedException e) {
			throw new ConnectionConnectException("Connection closed while performing handshake");
		}
	}

	private void sendNetinfo() throws IOException {
		final Cell cell = CellImpl.createCell(0, Cell.NETINFO);
		// XXX this is a mess
		Date now = new Date();
		cell.putInt((int)(now.getTime() / 1000));
		cell.putByte(4);
		cell.putByte(4);
		cell.putByteArray(connection.getRouter().getAddress().getAddressDataBytes());
		cell.putByte(1);
		cell.putByte(4);
		cell.putByte(4);
		cell.putInt(0);

		connection.sendCell(cell);

	}

	private void recvNetinfo() throws IOException {
		try {
			final Cell cell = connection.readConnectionControlCell();
			// XXX verify command == NETINFO

			remoteTimestamp = cell.getInt();
			myAddress = readAddress(cell);
			final int addressCount = cell.getByte();
			for(int i = 0; i < addressCount; i++)
				remoteAddresses.add(readAddress(cell));
		} catch (ConnectionClosedException e) {
			throw new ConnectionConnectException("Connection closed while performing handshake");
		}

		
	}

	private IPv4Address readAddress(Cell cell) {
		final int type = cell.getByte();
		final int len = cell.getByte();

		return new IPv4Address(cell.getInt());
	}

}
