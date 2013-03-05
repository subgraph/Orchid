package org.torproject.jtor.sockets;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

import org.torproject.jtor.TorClient;

public class JTorSocketFactory extends SocketFactory {

	private final TorClient torClient;
	private final boolean exceptionOnLocalBind;
	
	public JTorSocketFactory(TorClient torClient) {
		this(torClient, true);
	}

	public JTorSocketFactory(TorClient torClient, boolean exceptionOnLocalBind) {
		this.torClient = torClient;
		this.exceptionOnLocalBind = exceptionOnLocalBind;
	}

	@Override
	public Socket createSocket(String host, int port) throws IOException,
			UnknownHostException {
		return createJTorSocket(host, port);
	}

	@Override
	public Socket createSocket(String host, int port, InetAddress localHost,
			int localPort) throws IOException, UnknownHostException {
		if(exceptionOnLocalBind) {
			throw new UnsupportedOperationException("Cannot bind to local address");
		}
		return createSocket(host, port);
	}

	@Override
	public Socket createSocket(InetAddress address, int port) throws IOException {
		return createJTorSocket(address.getHostAddress(), port);
	}

	@Override
	public Socket createSocket(InetAddress address, int port,
			InetAddress localAddress, int localPort) throws IOException {
		if(exceptionOnLocalBind) {
			throw new UnsupportedOperationException("Cannot bind to local address");
		}
		return createSocket(address, port);
	}

	private Socket createJTorSocket(String host, int port) throws IOException {
		final JTorSocketImpl impl = new JTorSocketImpl(torClient);
		// call protected constructor
		final Socket s = new Socket(impl) {};
		final SocketAddress endpoint = InetSocketAddress.createUnresolved(host, port);
		s.connect(endpoint);
		return s;
	}
}
