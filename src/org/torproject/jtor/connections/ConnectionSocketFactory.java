package org.torproject.jtor.connections;

import java.io.IOException;

import java.lang.reflect.Constructor;

import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketImpl;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.torproject.jtor.TorException;

public class ConnectionSocketFactory {
	static final String[] V1_CIPHERS_ONLY = {
		"TLS_DHE_RSA_WITH_AES_256_CBC_SHA",
	    "TLS_DHE_RSA_WITH_AES_128_CBC_SHA",
	    "SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA",
	};
	
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
	
	private static SSLContext createSSLContext() {
		System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
		try {
			final SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, NULL_TRUST, null);
			return sslContext;
		} catch (NoSuchAlgorithmException e) {
			throw new TorException(e);
		} catch (KeyManagementException e) {
			throw new TorException(e);
		}
	}
	
	private final SSLSocketFactory socketFactory;
	
	ConnectionSocketFactory() {
		socketFactory = createSSLContext().getSocketFactory();
	}
	
	SSLSocket createSocket(SocketAddress address, int timeout) {
		try {
			Socket s = createOriginalSocket();
			s.connect(address, timeout);
			final SSLSocket socket = (SSLSocket) socketFactory.createSocket(s, null, -1, true);
			socket.setEnabledCipherSuites(MANDATORY_CIPHERS);
			socket.setUseClientMode(true);
			return socket;
		} catch (IOException e) {
			throw new TorException(e);
		}
	}

	Socket createOriginalSocket() throws IOException {
		try {
	        Class<?> clazz = Class.forName("java.net.SocksSocketImpl");
	        Constructor<?> constructor = clazz.getDeclaredConstructor();
	        // this maybe does not work because of security restrictions:
	        constructor.setAccessible(true);
	        SocketImpl impl = (SocketImpl)constructor.newInstance();
	        return new Socket(impl) {};
		} catch (Throwable t) {
			throw new IOException("Cannot create original socket", t);
		}
	}
}
