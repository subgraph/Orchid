package com.subgraph.orchid.connections;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.net.ssl.SSLSocket;

import com.subgraph.orchid.Cell;
import com.subgraph.orchid.ConnectionHandshakeException;
import com.subgraph.orchid.ConnectionIOException;
import com.subgraph.orchid.TorConfig;
import com.subgraph.orchid.circuits.cells.CellImpl;
import com.subgraph.orchid.data.IPv4Address;

public abstract class ConnectionHandshake {
	
	static ConnectionHandshake createHandshake(TorConfig config, ConnectionImpl connection, SSLSocket socket) throws ConnectionHandshakeException {
		if(config.getHandshakeV3Enabled() && ConnectionHandshakeV3.sessionSupportsHandshake(socket.getSession())) {
			return new ConnectionHandshakeV3(connection, socket);
		} else if(config.getHandshakeV2Enabled()) {
			return new ConnectionHandshakeV2(connection, socket);
		} else {
			throw new ConnectionHandshakeException("No valid handshake type available for this connection");
		}
			
	}
	
	protected final ConnectionImpl connection;
	protected final SSLSocket socket;
	
	protected final List<Integer> remoteVersions;
	private int remoteTimestamp;
	private IPv4Address myAddress;
	private final List<IPv4Address> remoteAddresses;

	ConnectionHandshake(ConnectionImpl connection, SSLSocket socket) {
		this.connection = connection;
		this.socket = socket;
		this.remoteVersions = new ArrayList<Integer>();
		this.remoteAddresses = new ArrayList<IPv4Address>();
	}

	abstract void runHandshake() throws IOException, InterruptedException, ConnectionIOException;
		
	int getRemoteTimestamp() {
		return remoteTimestamp;
	}

	IPv4Address getMyAddress() {
		return myAddress;
	}
	
	protected Cell expectCell(int expectedType) throws ConnectionHandshakeException {
		try {
			final Cell c = connection.readConnectionControlCell();
			if(c.getCommand() != expectedType) {
				throw new ConnectionHandshakeException("Expecting Cell command [ "+ expectedType + " ] and got [ "+ c.getCommand() +" ] instead");
			}
			return c;
		} catch (ConnectionIOException e) {
			throw new ConnectionHandshakeException("Connection exception while performing handshake "+ e);
		}
	}

	protected  void sendVersions(int... versions) throws ConnectionIOException {
		final Cell cell = CellImpl.createVarCell(0, Cell.VERSIONS, versions.length * 2);
		for(int v: versions) {
			cell.putShort(v);
		}
		connection.sendCell(cell);
	}

	protected void receiveVersions() throws ConnectionHandshakeException {
		final Cell c = expectCell(Cell.VERSIONS);
		while(c.cellBytesRemaining() >= 2) {
			remoteVersions.add(c.getShort());
		}
	}

	protected void sendNetinfo() throws ConnectionIOException {
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

	protected void recvNetinfo() throws ConnectionHandshakeException {
		final Cell cell = expectCell(Cell.NETINFO);
		remoteTimestamp = cell.getInt();
		myAddress = readAddress(cell);
		final int addressCount = cell.getByte();
		for(int i = 0; i < addressCount; i++) {
			IPv4Address addr = readAddress(cell);
			if(addr != null) {
				remoteAddresses.add(addr);
			}
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
