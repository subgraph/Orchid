package org.torproject.jtor.connections;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocket;

import org.torproject.jtor.circuits.Connection;
import org.torproject.jtor.circuits.ConnectionFailedException;
import org.torproject.jtor.circuits.ConnectionHandshakeException;
import org.torproject.jtor.circuits.ConnectionTimeoutException;
import org.torproject.jtor.circuits.impl.TorInitializationTracker;
import org.torproject.jtor.dashboard.DashboardRenderable;
import org.torproject.jtor.directory.Router;

public class ConnectionCache implements DashboardRenderable {
	private final static Logger logger = Logger.getLogger(ConnectionCache.class.getName());
	
	private class ConnectionTask implements Callable<ConnectionImpl> {

		private final Router router;
		private final boolean isDirectoryConnection;
		
		ConnectionTask(Router router, boolean isDirectoryConnection) {
			this.router = router;
			this.isDirectoryConnection = isDirectoryConnection;
		}

		public ConnectionImpl call() throws Exception {
			SSLSocket socket = factory.createSocket();
			ConnectionImpl conn = new ConnectionImpl(socket, router, initializationTracker, isDirectoryConnection);
			conn.connect();
			return conn;
		}
	}
	
	private class CloseIdleConnectionCheckTask implements Runnable {
		public void run() {
			for(Future<ConnectionImpl> f: activeConnections.values()) {
				if(f.isDone()) {
					try {
						ConnectionImpl c = f.get();
						c.idleCloseCheck();
					} catch (Exception e) { }
				}
			}
		}
	}

	private final ConcurrentMap<Router, Future<ConnectionImpl>> activeConnections = new ConcurrentHashMap<Router, Future<ConnectionImpl>>();
	private final ConnectionSocketFactory factory = new ConnectionSocketFactory();
	private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

	private TorInitializationTracker initializationTracker;
	
	public ConnectionCache(TorInitializationTracker tracker) {
		this.initializationTracker = tracker;
		scheduledExecutor.scheduleAtFixedRate(new CloseIdleConnectionCheckTask(), 5000, 5000, TimeUnit.MILLISECONDS);
	}

	public Connection getConnectionTo(Router router, boolean isDirectoryConnection) throws InterruptedException, ConnectionTimeoutException, ConnectionFailedException, ConnectionHandshakeException {
		logger.fine("Get connection to "+ router.getAddress() + " "+ router.getOnionPort() + " " + router.getNickname());
		while(true) {
			Future<ConnectionImpl> f = getFutureFor(router, isDirectoryConnection);
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
	
	
	public void dashboardRender(PrintWriter writer, int flags) throws IOException {
		writer.println("[Connection Cache]");
		writer.println();
		for(ConnectionImpl c: getActiveConnections()) {
			if(!c.isClosed()) {
				c.dashboardRender(writer, flags);
			}
		}
		writer.println();
	}

	public List<ConnectionImpl> getActiveConnections() {
		List<ConnectionImpl> cs = new ArrayList<ConnectionImpl>();
		for(Future<ConnectionImpl> future: activeConnections.values()) {
			ConnectionImpl connection = getConnectionFromFuture(future);
			if(connection != null) {
				cs.add(connection);
			}
		}
		return cs;
	}

	private ConnectionImpl getConnectionFromFuture(Future<ConnectionImpl> future) {
		if(!future.isDone() || future.isCancelled()) {
			return null;
		}
		try {
			return future.get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return null;
		} catch (ExecutionException e) {
			return null;
		}
	}

	private Future<ConnectionImpl> getFutureFor(Router router, boolean isDirectoryConnection) {
		Future<ConnectionImpl> f = activeConnections.get(router);
		if(f != null) {
			return f;
		}
		FutureTask<ConnectionImpl> ft = new FutureTask<ConnectionImpl>(new ConnectionTask(router, isDirectoryConnection));
		f = activeConnections.putIfAbsent(router, ft);
		if(f == null) {
			ft.run();
			return ft;
		}
		return f;
	}
}
