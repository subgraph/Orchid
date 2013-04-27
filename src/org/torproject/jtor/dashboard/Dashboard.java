package org.torproject.jtor.dashboard;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.torproject.jtor.data.IPv4Address;
import org.torproject.jtor.misc.GuardedBy;

/**
 * A debugging utility which displays continuously updated information about the internal state
 * of various components to clients which connect to a network port listening on localhost.
 */
public class Dashboard implements DashboardRenderable {
	private final static Logger logger = Logger.getLogger(Dashboard.class.getName());
	
	private final static int DEFAULT_LISTENING_PORT = 12345;
	private final static int DEFAULT_FLAGS = DASHBOARD_CIRCUITS | DASHBOARD_STREAMS;
	private final static IPv4Address LOCALHOST = IPv4Address.createFromString("127.0.0.1");
			
	@GuardedBy("this") private int listeningPort = DEFAULT_LISTENING_PORT;
	@GuardedBy("this") private int flags = DEFAULT_FLAGS;
	@GuardedBy("this") private ServerSocket listeningSocket;
	@GuardedBy("this") private boolean isListening;
	
	private final List<DashboardRenderable> renderables;
	private final Executor executor;
	
	
	public Dashboard() {
		renderables = new CopyOnWriteArrayList<DashboardRenderable>();
		renderables.add(this);
		executor = Executors.newCachedThreadPool();
	}
	
	public void addRenderables(Object...objects) {
		for(Object ob: objects) {
			if(ob instanceof DashboardRenderable) {
				renderables.add((DashboardRenderable) ob);
			}
		}
	}

	public void addRenderable(DashboardRenderable renderable) {
		renderables.add(renderable);
	}

	public synchronized void enableFlag(int flag) {
		flags |= flag;
	}
	
	public synchronized void disableFlag(int flag) {
		flags &= ~flag;
	}
	
	
	public synchronized boolean isEnabled(int f) {
		return (flags & f) != 0;
	}
	
	public synchronized void setListeningPort(int port) {
		if(port != listeningPort) {
			listeningPort = port;
			if(isListening) {
				stopListening();
				startListening();
			}
		}
	}
	
	public synchronized void startListening() {
		if(isListening) {
			return;
		}
		try {
			listeningSocket = new ServerSocket(listeningPort, 50, LOCALHOST.toInetAddress());
			isListening = true;
			executor.execute(createAcceptLoopRunnable(listeningSocket));
		} catch (IOException e) {
			logger.warning("Failed to create listening Dashboard socket on port "+ listeningPort +": "+ e);
		}
	}
	
	public synchronized void stopListening() {
		if(!isListening) {
			return;
		}
		if(listeningSocket != null) {
			closeQuietly(listeningSocket);
			listeningSocket = null;
		}
		isListening = false;
	}
	
	public synchronized boolean isListening() {
		return isListening;
	}

	private Runnable createAcceptLoopRunnable(final ServerSocket ss) {
		return new Runnable() {
			public void run() {
				acceptConnections(ss);
			}
		};
	}

	private void acceptConnections(ServerSocket ss) {
		while(true) {
			try {
				Socket s = ss.accept();
				executor.execute(new DashboardConnection(this, s));
			} catch (IOException e) {
				if(!ss.isClosed()) {
					logger.warning("IOException on dashboard server socket: "+ e);
				}
				stopListening();
				return;
			}
		}
	}
	
	void renderAll(PrintWriter writer) throws IOException {
		for(DashboardRenderable dr: renderables) {
			dr.dashboardRender(writer, flags);
		}
	}

	
	private void closeQuietly(Closeable closeable) {
		try {
			closeable.close();
		} catch (IOException e) { }
	}

	public void dashboardRender(PrintWriter writer, int flags) {
		writer.println("[Dashboard]");
		writer.println();
	}
}
