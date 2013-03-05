package org.torproject.jtor.xmlrpc;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcTransport;
import org.apache.xmlrpc.client.XmlRpcTransportFactory;
import org.torproject.jtor.TorClient;
import org.torproject.jtor.sockets.JTorSocketFactory;

public class JTorXmlRpcTransportFactory implements XmlRpcTransportFactory {
	private final XmlRpcClient client;
	private final SSLContext sslContext;
	private final SocketFactory socketFactory;
		
	public JTorXmlRpcTransportFactory(XmlRpcClient client, TorClient torClient) {
		this(client, torClient, null);
	}

	public JTorXmlRpcTransportFactory(XmlRpcClient client, TorClient torClient, SSLContext sslContext) {
		this.client = client;
		this.socketFactory = new JTorSocketFactory(torClient);
		this.sslContext = sslContext;
	}

	public XmlRpcTransport getTransport() {
		return new JTorXmlRpcTransport(client, socketFactory, sslContext);
	}
}
