package com.subgraph.orchid.connections;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

import com.subgraph.orchid.ConnectionHandshakeException;
import com.subgraph.orchid.ConnectionIOException;
import com.subgraph.orchid.Router;
import com.subgraph.orchid.crypto.TorPublicKey;

/**
 * This class performs a Version 2 handshake as described in section 2 of
 * tor-spec.txt.  The handshake is considered complete after VERSIONS and
 * NETINFO cells have been exchanged between the two sides.
 */
public class ConnectionHandshakeV2 extends ConnectionHandshake {

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
	
	ConnectionHandshakeV2(ConnectionImpl connection, SSLSocket socket) {
		super(connection, socket);
	}

	void runHandshake() throws IOException, InterruptedException, ConnectionIOException {
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
		sendVersions(2);
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
			if(chain.length != 2) {
				throw new ConnectionHandshakeException("Expecting 2 certificate chain from router and received chain length "+ chain.length);
			}
			chain[0].verify(chain[1].getPublicKey());
			return chain[1];
		} catch (SSLPeerUnverifiedException e) {
			throw new ConnectionHandshakeException("No certificates received from router");
		} catch (GeneralSecurityException e) {
			throw new ConnectionHandshakeException("Incorrect signature on certificate chain");
		} catch (CertificateException e) {
			throw new ConnectionHandshakeException("Malformed certificate received");
		}
	}
}
