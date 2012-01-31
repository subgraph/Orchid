package org.torproject.jtor.socks.impl;

import java.io.IOException;
import java.net.Socket;

import org.torproject.jtor.TorException;

public class Socks5Request extends SocksRequest {
	final static int SOCKS5_VERSION = 5;
	final static int SOCKS5_AUTH_NONE = 0;
	final static int SOCKS5_COMMAND_CONNECT = 1;
	final static int SOCKS5_ADDRESS_IPV4 = 1;
	final static int SOCKS5_ADDRESS_HOSTNAME = 3;
	final static int SOCKS5_ADDRESS_IPV6 = 4;
	final static int SOCKS5_STATUS_SUCCESS = 0;
	final static int SOCKS5_STATUS_FAILURE = 1;
	final static int SOCKS5_STATUS_CONNECTION_REFUSED = 5;
	
	private int command;
	private int addressType;
	private byte[] addressBytes;
	private byte[] portBytes;
	
	Socks5Request(Socket socket) {
		super(socket);
	}
	
	public boolean isConnectRequest() {
		return command == SOCKS5_COMMAND_CONNECT;
	}
	
	private String addressBytesToHostname() {
		if(addressType != SOCKS5_ADDRESS_HOSTNAME)
			throw new TorException("SOCKS 4 request is not a hostname request");
		final StringBuilder sb = new StringBuilder();
		for(int i = 1; i < addressBytes.length; i++) {
			char c = (char) (addressBytes[i] & 0xFF);
			sb.append(c);
		}
		return sb.toString();
	}
	
	public void readRequest() {
		processAuthentication();
		if(readByte() != SOCKS5_VERSION)
			throw new SocksRequestException();

		command = readByte();
		readByte(); // Reserved
		addressType = readByte();
		addressBytes = readAddressBytes();
		portBytes = readPortData();
		if(addressType == SOCKS5_ADDRESS_IPV4)
			setIPv4AddressData(addressBytes);
		else if(addressType == SOCKS5_ADDRESS_HOSTNAME)
			setHostname(addressBytesToHostname());
		else 
			throw new SocksRequestException();
		setPortData(portBytes);		
	}
	
	public void sendConnectionRefused() throws IOException {
		sendResponse(SOCKS5_STATUS_CONNECTION_REFUSED);
	}

	public void sendError() throws IOException {
		sendResponse(SOCKS5_STATUS_FAILURE);
	}
	
	public void sendSuccess() throws IOException {
		sendResponse(SOCKS5_STATUS_SUCCESS);
	}
	
	private void sendResponse(int status) throws IOException {
		final int responseLength = 4 + addressBytes.length + portBytes.length;
		final byte[] response = new byte[responseLength];
		response[0] = SOCKS5_VERSION;
		response[1] = (byte) status;
		response[2] = 0;
		response[3] = (byte) addressType;
		System.arraycopy(addressBytes, 0, response, 4, addressBytes.length);
		System.arraycopy(portBytes, 0, response, 4 + addressBytes.length, portBytes.length);
		socketWrite(response);
	}
	
	private void processAuthentication() {
		final int nmethods = readByte();
		boolean foundAuthNone = false;
		for(int i = 0; i < nmethods; i++) {
			final int meth = readByte();
			if(meth == SOCKS5_AUTH_NONE)
				foundAuthNone = true;
		}
		final byte[] response = new byte[2];
		response[0] = SOCKS5_VERSION;
		response[1] = SOCKS5_AUTH_NONE;
		
		try {
			socketWrite(response);
		} catch (IOException e) {
			throw new SocksRequestException(e);
		}
	}
	
	private byte[] readAddressBytes() {
		switch(addressType) {
		case SOCKS5_ADDRESS_IPV4:
			return readIPv4AddressData();
		case SOCKS5_ADDRESS_IPV6:
			return readIPv6AddressData();
		case SOCKS5_ADDRESS_HOSTNAME:
			return readHostnameData();
		default:
			throw new SocksRequestException();
		}
	}
	
	private byte[] readHostnameData() {
		final int length = readByte();
		final byte[] addrData = new byte[length + 1];
		addrData[0] = (byte) length;
		readAll(addrData, 1, length);
		return addrData;
	}
}
