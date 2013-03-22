package org.torproject.jtor.connections;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocket;

import org.torproject.jtor.circuits.Connection;
import org.torproject.jtor.circuits.ConnectionFailedException;
import org.torproject.jtor.circuits.ConnectionHandshakeException;
import org.torproject.jtor.circuits.ConnectionTimeoutException;
import org.torproject.jtor.directory.Router;

public class ConnectionCache {
	private final static Logger logger = Logger.getLogger(ConnectionCache.class.getName());
	
	private class ConnectionTask implements Callable<ConnectionImpl> {

		private final Router router;
		
		ConnectionTask(Router router) {
			this.router = router;
		}

		public ConnectionImpl call() throws Exception {
			SSLSocket socket = factory.createSocket();
			ConnectionImpl conn = new ConnectionImpl(socket, router);
			conn.connect();
			return conn;
		}
	}
	private final ConcurrentMap<Router, Future<ConnectionImpl>> activeConnections = new ConcurrentHashMap<Router, Future<ConnectionImpl>>();
	private final ConnectionSocketFactory factory = new ConnectionSocketFactory();
	
	public Connection getConnectionTo(Router router) throws InterruptedException, ConnectionTimeoutException, ConnectionFailedException, ConnectionHandshakeException {
		logger.fine("Get connection to "+ router.getAddress() + " "+ router.getOnionPort() + " " + router.getNickname());
		while(true) {
			Future<ConnectionImpl> f = getFutureFor(router);
			try {
				ConnectionImpl c = f.get();
				if(c.isClosed()) {
					activeConnections.remove(router, f);
				} else {
					return c;
				}
			} catch (CancellationException e) {
				activeConnections.remove(router, f);
			} catch (ExecutionException e) {
				activeConnections.remove(router, f);
				final Throwable t = e.getCause();
				if(t instanceof ConnectionTimeoutException) {
					throw (ConnectionTimeoutException) t;
				} else if(t instanceof ConnectionFailedException) {
					throw (ConnectionFailedException) t;
				} else if(t instanceof ConnectionHandshakeException) {
					throw (ConnectionHandshakeException) t;
				}
				throw new RuntimeException("Unexpected exception: "+ e, e);
			}
		}
	}
	
	private Future<ConnectionImpl> getFutureFor(Router router) {
		Future<ConnectionImpl> f = activeConnections.get(router);
		if(f != null) {
			return f;
		}
		FutureTask<ConnectionImpl> ft = new FutureTask<ConnectionImpl>(new ConnectionTask(router));
		f = activeConnections.putIfAbsent(router, ft);
		if(f == null) {
			ft.run();
			return ft;
		}
		return f;
	}
}
