package com.subgraph.orchid.connections;

import java.io.IOException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.security.cert.X509Certificate;

import com.subgraph.orchid.Cell;
import com.subgraph.orchid.ConnectionHandshakeException;
import com.subgraph.orchid.ConnectionIOException;
import com.subgraph.orchid.Router;
import com.subgraph.orchid.circuits.cells.CellImpl;
import com.subgraph.orchid.crypto.TorPublicKey;
import com.subgraph.orchid.data.IPv4Address;

/**
 * This class performs a Version 2 handshake as described in section 2 of
 * tor-spec.txt.  The handshake is considered complete after VERSIONS and
 * NETINFO cells have been exchanged between the two sides.
 */
public class ConnectionHandshakeV2 {
	private static class HandshakeFinishedMonitor implements HandshakeCompletedListener {
		final Object lock = new Object();
		boolean isFinished;

		public void handshakeCompleted(HandshakeCompletedEvent event) {
			synchronized(lock) {
				this.isFinished = true;
				lock.notifyAll();
			}
		}
	
		public void waitFinished() throws InterruptedException {
			synchronized(lock) {
				while(!isFinished) {
					lock.wait();
				}
			}
		}
	}
	private final static int[] SUPPORTED_CONNECTION_VERSIONS = {2};

	private final ConnectionImpl connection;
	private final SSLSocket socket;

	private final List<Integer> remoteVersions;
	private int remoteTimestamp;
	private IPv4Address myAddress;
	private final List<IPv4Address> remoteAddresses;

	ConnectionHandshakeV2(ConnectionImpl connection, SSLSocket socket) {
		this.connection = connection;
		this.socket = socket;
		this.remoteVersions = new ArrayList<Integer>();
		this.remoteAddresses = new ArrayList<IPv4Address>();
	}

	void runHandshake() throws IOException, InterruptedException, ConnectionIOException {
		socket.startHandshake();
		
		// Swap in V1-only ciphers for second handshake as a workaround for:
		//
		//     https://trac.torproject.org/projects/tor/ticket/4591
		// 
		socket.setEnabledCipherSuites(ConnectionSocketFactory.V1_CIPHERS_ONLY);
		
		final HandshakeFinishedMonitor monitor = new HandshakeFinishedMonitor();
		socket.addHandshakeCompletedListener(monitor);
		socket.startHandshake();
		monitor.waitFinished();
		socket.removeHandshakeCompletedListener(monitor);
		verifyIdentity(connection.getRouter(), socket.getSession());
		sendVersions();
		receiveVersions();
		sendNetinfo();
		recvNetinfo();
	}

	private void verifyIdentity(Router router, SSLSession session) throws ConnectionHandshakeException {
		final X509Certificate c = getIdentityCertificateFromSession(session);
		final PublicKey publicKey = c.getPublicKey();
		if(!(publicKey instanceof RSAPublicKey)) {
			throw new ConnectionHandshakeException("Certificate public key is not an RSA key as expected");
		}
		final TorPublicKey certKey = new TorPublicKey((RSAPublicKey) publicKey);
		if(!certKey.getFingerprint().equals(router.getIdentityHash())) {
			throw new ConnectionHandshakeException("Router identity key does not match certicate key");
		}
	}
	
	private X509Certificate getIdentityCertificateFromSession(SSLSession session) throws ConnectionHandshakeException {
		try {
			X509Certificate[] chain = session.getPeerCertificateChain();
			if(chain.length == 0) {
				throw new ConnectionHandshakeException("No certificates received from router");
			} else if (chain.length == 1) {
				return chain[0];
			} else {
				return chain[1];
			}
		} catch (SSLPeerUnverifiedException e) {
			throw new ConnectionHandshakeException("No certificates received from router");
		}
	}
	
	int getRemoteTimestamp() {
		return remoteTimestamp;
	}

	IPv4Address getMyAddress() {
		return myAddress;
	}

	private  void sendVersions() throws ConnectionIOException {
		final Cell cell = CellImpl.createVarCell(0, Cell.VERSIONS, SUPPORTED_CONNECTION_VERSIONS.length * 2);
		for(int v: SUPPORTED_CONNECTION_VERSIONS) {
			cell.putShort(v);
		}
		connection.sendCell(cell);
	}

	private void receiveVersions() throws ConnectionHandshakeException {
		try {
			Cell c  = connection.readConnectionControlCell();
			if(c.getCommand() != Cell.VERSIONS) {
				throw new ConnectionHandshakeException("Expecting VERSIONS cell and got command = "+ c.getCommand() + " instead");
			}
			while(c.cellBytesRemaining() >= 2)
				remoteVersions.add(c.getShort());
		} catch (ConnectionIOException e) {
			throw new ConnectionHandshakeException("Connection exception while performing handshake "+ e);
		}
	}

	private void sendNetinfo() throws ConnectionIOException {
		final Cell cell = CellImpl.createCell(0, Cell.NETINFO);
		putTimestamp(cell);
		putIPv4Address(cell, connection.getRouter().getAddress());
		putMyAddresses(cell);
		connection.sendCell(cell);
	}

	private void putTimestamp(Cell cell) {
		final Date now = new Date();
		cell.putInt((int) (now.getTime() / 1000));
	}

	private void putIPv4Address(Cell cell, IPv4Address address) {
		final byte[] data = address.getAddressDataBytes();
		cell.putByte(Cell.ADDRESS_TYPE_IPV4);
		cell.putByte(data.length); 
		cell.putByteArray(data);
	}
	
	private void putMyAddresses(Cell cell) {
		cell.putByte(1);
		putIPv4Address(cell, new IPv4Address(0));
	}

	private void recvNetinfo() throws ConnectionHandshakeException {
		try {
			final Cell cell = connection.readConnectionControlCell();
			if(cell.getCommand() != Cell.NETINFO) {
				throw new ConnectionHandshakeException("Expecting NETINFO Cell, got command = "+ cell.getCommand() + " instead");
			}
			remoteTimestamp = cell.getInt();
			myAddress = readAddress(cell);
			final int addressCount = cell.getByte();
			for(int i = 0; i < addressCount; i++) {
				IPv4Address addr = readAddress(cell);
				if(addr != null) {
					remoteAddresses.add(addr);
				}
			}
		} catch (ConnectionIOException e) {
			throw new ConnectionHandshakeException("Connection closed while performing handshake "+ e);
		}
	}

	private IPv4Address readAddress(Cell cell) {
		final int type = cell.getByte();
		final int len = cell.getByte();
		if(type == Cell.ADDRESS_TYPE_IPV4 && len == 4) {
			return new IPv4Address(cell.getInt());
		}
		final byte[] buffer = new byte[len];
		cell.getByteArray(buffer);
		return null;
	}
}
