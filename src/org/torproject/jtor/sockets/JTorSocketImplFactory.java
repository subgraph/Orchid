package org.torproject.jtor.sockets;

import java.net.SocketImpl;
import java.net.SocketImplFactory;

import org.torproject.jtor.TorClient;

public class JTorSocketImplFactory implements SocketImplFactory {
	private final TorClient torClient;
	
	public JTorSocketImplFactory(TorClient torClient) {
		this.torClient = torClient;
	}

	public SocketImpl createSocketImpl() {
		return new JTorSocketImpl(torClient);
	}
}
