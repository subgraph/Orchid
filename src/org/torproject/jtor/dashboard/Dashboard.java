package org.torproject.jtor.dashboard;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.torproject.jtor.data.IPv4Address;

public class Dashboard implements DashboardRenderable {
	
	private final static Logger logger = Logger.getLogger(Dashboard.class.getName());
	private final static int DEFAULT_LISTENING_PORT = 12345;
	private final IPv4Address localhost = IPv4Address.createFromString("127.0.0.1");
			
	private int listeningPort = DEFAULT_LISTENING_PORT;

	private int flags;


	private ServerSocket listeningSocket;
	private boolean isListening;
	
	private final List<DashboardRenderable> renderables;
	private final Executor executor;
	
	
	public Dashboard() {
		renderables = new ArrayList<DashboardRenderable>();
		renderables.add(this);
		executor = Executors.newCachedThreadPool();
	}
	
	public void addRenderables(Object...objects) {
		for(Object ob: objects) {
			if(ob instanceof DashboardRenderable) {
				addRenderable((DashboardRenderable) ob);
			}
		}
	}

	public void addRenderable(DashboardRenderable renderable) {
		synchronized (renderables) {
			renderables.add(renderable);
		}
	}

	public void enableFlag(int flag) {
		flags |= flag;
	}
	
	public void disableFlag(int flag) {
		flags &= ~flag;
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
			listeningSocket = new ServerSocket(listeningPort, 50, localhost.toInetAddress());
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
				executor.execute(createHandleConnectionRunnable(s));
			} catch (IOException e) {
				if(!ss.isClosed()) {
					e.printStackTrace();
				}
				stopListening();
				return;
			}
		}
	}
	
	private Runnable createHandleConnectionRunnable(final Socket s) {
		return new Runnable() {
			public void run() {
				runConnection(s);
			}
		};
	}
	
	private void runConnection(Socket s) {
		try {
			final PrintWriter writer = new PrintWriter(s.getOutputStream());
			for(DashboardRenderable dr: renderables) {
				dr.dashboardRender(writer, flags);
			}
			writer.flush();
		} catch (IOException e) {
			logger.warning("IO error handling dashboard connection "+ e);
		} finally {
			closeQuietly(s);
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
