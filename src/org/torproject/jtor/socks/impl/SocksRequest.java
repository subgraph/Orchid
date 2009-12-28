package org.torproject.jtor.socks.impl;

import java.io.IOException;
import java.net.Socket;

import org.torproject.jtor.data.IPv4Address;

public abstract class SocksRequest {
	
	private final Socket socket;
	
	private byte[] addressData;
	private IPv4Address address;
	private String hostname;
	private int port;
	
	protected SocksRequest(Socket socket) {
		this.socket = socket;
	}
	
	abstract public void readRequest();
	abstract public boolean isConnectRequest();
	abstract void sendError() throws IOException;
	abstract void sendSuccess() throws IOException;
	abstract void sendConnectionRefused() throws IOException;
	
	public int getPort() {
		return port;
	}
	
	public IPv4Address getAddress() {
		return address;
	}
	
	public boolean hasHostname() {
		return hostname != null;
	}
	
	public String getHostname() {
		return hostname;
	}
	
	protected void setPortData(byte[] data) {
		if(data.length != 2)
			throw new SocksRequestException();
		port = (data[0] & 0xFF << 8) | (data[1] & 0xFF);
	}
	
	protected void setIPv4AddressData(byte[] data) {
		if(data.length != 4)
			throw new SocksRequestException();
		addressData = data;
		
		int addressValue = 0;
		for(byte b: addressData) {
			addressValue <<= 8;
			addressValue |= (b & 0xFF);
		}
		address = new IPv4Address(addressValue);
	}
	
	protected void setHostname(String name) {
		hostname = name;
	}
	
	protected byte[] readPortData() {
		final byte[] data = new byte[2];
		readAll(data, 0, 2);
		return data;
	}
	
	protected byte[] readIPv4AddressData() {
		final byte[] data = new byte[4];
		readAll(data);
		return data;
	}
	
	protected byte[] readIPv6AddressData() {
		final byte[] data = new byte[16];
		readAll(data);
		return data;
	}
	
	protected String readNullTerminatedString() {
		try {
			final StringBuilder sb = new StringBuilder();
			while(true) {
				final int c = socket.getInputStream().read();
				if(c == -1)
					throw new SocksRequestException();
				if(c == 0)
					return sb.toString();
				char ch = (char) c;
				sb.append(ch);
			}
		} catch (IOException e) {
			throw new SocksRequestException(e);
		}
	}
	
	protected int readByte() {
		try {
			final int n = socket.getInputStream().read();
			if(n == -1)
				throw new SocksRequestException();
			return n;
		} catch (IOException e) {
			throw new SocksRequestException(e);
		}
	}
	
	protected void readAll(byte[] buffer) {
		readAll(buffer, 0, buffer.length);
	}
	
	protected void readAll(byte[] buffer, int offset, int length) {
		try {
			while(length > 0) {
				int n = socket.getInputStream().read(buffer, offset, length);
				if(n == -1)
					throw new SocksRequestException();
				offset += n;
				length -= n;
			}
		} catch (IOException e) {
			throw new SocksRequestException(e);
		}
	}
	
	protected void socketWrite(byte[] buffer) throws IOException {
		socket.getOutputStream().write(buffer);
	}

}
