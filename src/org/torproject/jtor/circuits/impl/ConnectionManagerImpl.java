package org.torproject.jtor.circuits.impl;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.HashMap;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.torproject.jtor.TorException;
import org.torproject.jtor.directory.RouterDescriptor;

public class ConnectionManagerImpl {
	private static final String[] MANDATORY_CIPHERS = {
		"TLS_DHE_RSA_WITH_AES_256_CBC_SHA",
	    "TLS_DHE_RSA_WITH_AES_128_CBC_SHA",
	    "SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA",
	    "SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA"};

	private static final TrustManager[] NULL_TRUST = {
		new X509TrustManager() {
			private final X509Certificate[] empty = {};
			public void checkClientTrusted(X509Certificate[] chain, String authType)
					throws CertificateException {				
			}

			public void checkServerTrusted(X509Certificate[] chain, String authType)
					throws CertificateException {
			}

			public X509Certificate[] getAcceptedIssuers() {
				return empty;
			}
		}
	};
	
	private final Map<RouterDescriptor, ConnectionImpl> activeConnections;
	X509V3CertificateGenerator b;

	private final SSLContext sslContext;
	private SSLSocketFactory socketFactory;
	
	public ConnectionManagerImpl() {
		try {
			sslContext = SSLContext.getInstance("SSLv3");
			sslContext.init(null, NULL_TRUST, null);
		} catch (NoSuchAlgorithmException e) {
			throw new TorException(e);
		} catch (KeyManagementException e) {
			throw new TorException(e);
		}
		socketFactory = sslContext.getSocketFactory();
		activeConnections = new HashMap<RouterDescriptor, ConnectionImpl>();
	}
	
	public ConnectionImpl createConnection(RouterDescriptor router) {
		final SSLSocket socket = createSocket();
		socket.setEnabledCipherSuites(MANDATORY_CIPHERS);
		socket.setUseClientMode(true);
		return new ConnectionImpl(this, socket, router);
	}
	
	SSLSocket createSocket() {
		try {
			return (SSLSocket) socketFactory.createSocket();
		} catch (IOException e) {
			throw new TorException(e);
		}
	}
	
	void addActiveConnection(ConnectionImpl connection) {
		synchronized(activeConnections) {
			activeConnections.put(connection.getRouter(), connection);
		}
	}
	
	void removeActiveConnection(ConnectionImpl connection) {
		synchronized(activeConnections) {
			activeConnections.remove(connection);
		}
	}
	
	public ConnectionImpl findActiveLinkForRouter(RouterDescriptor router) {
		synchronized(activeConnections) {
			return activeConnections.get(router);
		}
		
	}

}
