package com.subgraph.orchid.socks;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import com.subgraph.orchid.CircuitManager;
import com.subgraph.orchid.SocksPortListener;
import com.subgraph.orchid.TorConfig;
import com.subgraph.orchid.TorException;

public class SocksPortListenerImpl implements SocksPortListener {
	private final static Logger logger = Logger.getLogger(SocksPortListenerImpl.class.getName());
	private final Set<Integer> listeningPorts = new HashSet<Integer>();
	private final Map<Integer, Thread> acceptThreads = new HashMap<Integer, Thread>();
	private final TorConfig config;
	private final CircuitManager circuitManager;
	private final Executor executor;
	
	public SocksPortListenerImpl(TorConfig config, CircuitManager circuitManager) {
		this.config = config;
		this.circuitManager = circuitManager;
		executor = Executors.newCachedThreadPool();
	}

	public void addListeningPort(int port) {
		if(port <= 0 || port > 65535)
			throw new TorException("Illegal listening port: "+ port);
		
		synchronized(listeningPorts) {
			if(listeningPorts.contains(port))
				return;
			listeningPorts.add(port);
			try {
				startListening(port);
				logger.fine("Listening for SOCKS connections on port "+ port);
			} catch (IOException e) {
				listeningPorts.remove(port);
				throw new TorException("Failed to listen on port "+ port +" : "+ e.getMessage());
			}
		}
		
	}
	
	private void startListening(int port) throws IOException {
		final ServerSocket ss = new ServerSocket(port);
		final Thread listeningThread = createAcceptThread(ss, port);
		acceptThreads.put(port, listeningThread);
		listeningThread.start();
	}
	
	private Thread createAcceptThread(final ServerSocket ss, final int port) {
		return new Thread(new Runnable() { public void run() {
			try {
				runAcceptLoop(ss);
			} catch (IOException e) {
				logger.warning("System error accepting SOCKS socket connections: "+ e.getMessage());
				synchronized(listeningPorts) {
					listeningPorts.remove(port);
					acceptThreads.remove(port);
				}
			}				
		}});
	}
	
	private void runAcceptLoop(ServerSocket ss) throws IOException {
		while(true) {
			final Socket s = ss.accept();
			executor.execute(newClientSocket(s));
		}
	}
	
	private Runnable newClientSocket(final Socket s) {
		return new SocksClientTask(config, s, circuitManager);
	}
}
