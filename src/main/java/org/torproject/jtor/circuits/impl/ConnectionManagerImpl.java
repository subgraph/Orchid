package org.torproject.jtor.circuits.impl;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.torproject.jtor.TorException;
import org.torproject.jtor.directory.Router;
import org.torproject.jtor.logging.LogManager;
import org.torproject.jtor.logging.Logger;

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

	private final Map<Router, ConnectionImpl> activeConnections;

	private final SSLContext sslContext;
	private final Logger logger;
	private SSLSocketFactory socketFactory;

	public ConnectionManagerImpl(LogManager logManager) {
		// See: http://java.sun.com/javase/javaseforbusiness/docs/TLSReadme.html
		System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
		
		try {
			sslContext = SSLContext.getInstance("SSLv3");
			sslContext.init(null, NULL_TRUST, null);
		} catch (NoSuchAlgorithmException e) {
			throw new TorException(e);
		} catch (KeyManagementException e) {
			throw new TorException(e);
		}
		socketFactory = sslContext.getSocketFactory();
		activeConnections = new HashMap<Router, ConnectionImpl>();
		this.logger = logManager.getLogger("connections");
	}

	public ConnectionImpl createConnection(Router router) {
		final SSLSocket socket = createSocket();
		socket.setEnabledCipherSuites(MANDATORY_CIPHERS);
		socket.setUseClientMode(true);
		return new ConnectionImpl(this, logger, socket, router);
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
			activeConnections.remove(connection.getRouter());
		}
	}

	public ConnectionImpl findActiveLinkForRouter(Router router) {
		synchronized(activeConnections) {
			return activeConnections.get(router);
		}
	}

}
