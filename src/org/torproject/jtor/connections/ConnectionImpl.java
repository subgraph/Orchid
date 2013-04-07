package org.torproject.jtor.connections;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocket;

import org.torproject.jtor.Tor;
import org.torproject.jtor.TorException;
import org.torproject.jtor.circuits.Circuit;
import org.torproject.jtor.circuits.Connection;
import org.torproject.jtor.circuits.ConnectionFailedException;
import org.torproject.jtor.circuits.ConnectionHandshakeException;
import org.torproject.jtor.circuits.ConnectionIOException;
import org.torproject.jtor.circuits.ConnectionTimeoutException;
import org.torproject.jtor.circuits.cells.Cell;
import org.torproject.jtor.circuits.impl.CellImpl;
import org.torproject.jtor.circuits.impl.TorInitializationTracker;
import org.torproject.jtor.crypto.TorRandom;
import org.torproject.jtor.dashboard.DashboardRenderable;
import org.torproject.jtor.directory.Router;

/**
 * This class represents a transport link between two onion routers or
 * between an onion proxy and an entry router.
 *
 */
public class ConnectionImpl implements Connection, DashboardRenderable {
	private final static Logger logger = Logger.getLogger(ConnectionImpl.class.getName());
	private final static int CONNECTION_IDLE_TIMEOUT = 5 * 60 * 1000; // 5 minutes
	private final static int DEFAULT_CONNECT_TIMEOUT = 5000;
	private final static Cell connectionClosedSentinel = CellImpl.createCell(0, 0);

	private final SSLSocket socket;
	private InputStream input;
	private OutputStream output;
	private final Router router;
	private final Map<Integer, Circuit> circuitMap;
	private final BlockingQueue<Cell> connectionControlCells;
	private final TorInitializationTracker initializationTracker;
	private final boolean isDirectoryConnection;
	
	private int currentId = 1;
	private boolean isConnected;
	private volatile boolean isClosed;
	private final Thread readCellsThread;
	private final Object connectLock = new Object();
	private Date lastActivity;

	public ConnectionImpl(SSLSocket socket, Router router, TorInitializationTracker tracker, boolean isDirectoryConnection) {
		this.socket = socket;
		this.router = router;
		this.circuitMap = new HashMap<Integer, Circuit>();
		this.readCellsThread = new Thread(createReadCellsRunnable());
		this.readCellsThread.setDaemon(true);
		this.connectionControlCells = new LinkedBlockingQueue<Cell>();
		this.initializationTracker = tracker;
		this.isDirectoryConnection = isDirectoryConnection;
		initializeCurrentCircuitId();
	}
	
	private void initializeCurrentCircuitId() {
		final TorRandom random = new TorRandom();
		currentId = random.nextInt(0xFFFF) + 1;
	}

	public Router getRouter() {
		return router;
	}

	public boolean isClosed() {
		return isClosed;
	}

	public int allocateCircuitId(Circuit circuit) {
		synchronized(circuitMap) {
			while(circuitMap.containsKey(currentId)) 
				incrementNextId();
			circuitMap.put(currentId, circuit);
			return currentId;
		}
	}

	private void incrementNextId() {
		currentId++;
		if(currentId > 0xFFFF)
			currentId = 1;
	}

	public boolean isConnected() {
		return isConnected;
	}

	public void connect() throws ConnectionFailedException, ConnectionTimeoutException, ConnectionHandshakeException {
		synchronized (connectLock) {
			if(isConnected) {
				return;
			}
			try {
				doConnect();
			} catch (SocketTimeoutException e) {
				throw new ConnectionTimeoutException();
			} catch (IOException e) {
				throw new ConnectionFailedException(e.getClass().getName() + " : "+ e.getMessage());
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new ConnectionHandshakeException("Handshake interrupted");
			} catch (ConnectionHandshakeException e) { 
				throw e;
			} catch (ConnectionIOException e) {
				throw new ConnectionFailedException(e.getMessage());
			}
			isConnected = true;
		}
	}

	private void doConnect() throws IOException, InterruptedException, ConnectionIOException {
		connectSocket();
		final ConnectionHandshakeV2 handshake = new ConnectionHandshakeV2(this, socket);		
		input = socket.getInputStream();
		output = socket.getOutputStream();
		readCellsThread.start();
		handshake.runHandshake();
		updateLastActivity();
	}
	
	private void connectSocket() throws IOException {
		if(initializationTracker != null) {
			if(isDirectoryConnection) {
				initializationTracker.notifyEvent(Tor.BOOTSTRAP_STATUS_CONN_DIR);
			} else {
				initializationTracker.notifyEvent(Tor.BOOTSTRAP_STATUS_CONN_OR);
			}
		}

		socket.connect(routerToSocketAddress(router), DEFAULT_CONNECT_TIMEOUT);
		
		if(initializationTracker != null) {
			if(isDirectoryConnection) {
				initializationTracker.notifyEvent(Tor.BOOTSTRAP_STATUS_HANDSHAKE_DIR);
			} else {
				initializationTracker.notifyEvent(Tor.BOOTSTRAP_STATUS_HANDSHAKE_OR);
			}
		}
	}

	private SocketAddress routerToSocketAddress(Router router) {
		final InetAddress address = router.getAddress().toInetAddress();
		return new InetSocketAddress(address, router.getOnionPort());
	}

	public void sendCell(Cell cell) throws ConnectionIOException  {
		if(!socket.isConnected()) {
			throw new ConnectionIOException("Cannot send cell because connection is not connected");
		}
		updateLastActivity();
		synchronized(output) {
			try {
				output.write(cell.getCellBytes());
			} catch (IOException e) {
				logger.fine("IOException writing cell to connection "+ e.getMessage());
				closeSocket();
				throw new ConnectionIOException(e.getClass().getName() + " : "+ e.getMessage());
			}
		}
	}

	private Cell recvCell() throws ConnectionIOException {
		try {
			return CellImpl.readFromInputStream(input);
		} catch(EOFException e) {
			closeSocket();
			throw new ConnectionIOException();
		} catch (IOException e) {
			if(!isClosed) {
				logger.fine("IOException reading cell from connection "+ this + " : "+ e.getMessage());
				closeSocket();
			}
			throw new ConnectionIOException(e.getClass().getName() + " " + e.getMessage());
		}
	}

	private void closeSocket() {
		try {
			logger.fine("Closing connection to "+ this);
			isClosed = true;
			socket.close();
			isConnected = false;
		} catch (IOException e) {
			logger.warning("Error closing socket: "+ e.getMessage());
		}
	}

	private Runnable createReadCellsRunnable() {
		return new Runnable() {
			public void run() {
				try {
					readCellsLoop();
				} catch(Exception e) {
					logger.log(Level.WARNING, "Unhandled exception processing incoming cells on connection "+ e, e);
				}
			}
		};
	}

	private void readCellsLoop() {
		while(!Thread.interrupted()) {
			try {
				processCell( recvCell() );
			} catch(ConnectionIOException e) {
				connectionControlCells.add(connectionClosedSentinel);
				notifyCircuitsLinkClosed();
				return;
			} catch(TorException e) {
				logger.log(Level.WARNING, "Unhandled Tor exception reading and processing cells: "+ e.getMessage(), e);
			}
		}
	}

	private void notifyCircuitsLinkClosed() {
		
	}

	Cell readConnectionControlCell() throws ConnectionIOException {
		try {
			return connectionControlCells.take();
		} catch (InterruptedException e) {
			closeSocket();
			throw new ConnectionIOException();
		}
	}

	private void processCell(Cell cell) {
		updateLastActivity();
		final int command = cell.getCommand();

		if(command == Cell.RELAY) {
			processRelayCell(cell);
			return;
		}
		switch(command) {
		case Cell.NETINFO:
		case Cell.VERSIONS:
			connectionControlCells.add(cell);
			break;

		case Cell.CREATED:
		case Cell.CREATED_FAST:
		case Cell.DESTROY:
			processControlCell(cell);
			break;
		default:
			// Ignore everything else
			break;
		}
	}

	private void processRelayCell(Cell cell) {
		synchronized(circuitMap) {
			final Circuit circuit = circuitMap.get(cell.getCircuitId());
			if(circuit == null) {
				logger.warning("Could not deliver relay cell for circuit id = "+ cell.getCircuitId() +" on connection "+ this +". Circuit not found");
				return;
			}
			circuit.deliverRelayCell(cell);
		}
	}

	private void processControlCell(Cell cell) {
		synchronized(circuitMap) {
			final Circuit circuit = circuitMap.get(cell.getCircuitId());
			if(circuit != null) {
				circuit.deliverControlCell(cell);
			}
		}
	}

	void idleCloseCheck() {
		synchronized (circuitMap) {
			final boolean needClose =  (!isClosed && circuitMap.isEmpty() && getIdleMilliseconds() > CONNECTION_IDLE_TIMEOUT);
			if(needClose) {
				logger.fine("Closing connection to "+ this +" on idle timeout");
				closeSocket();
			} 
		}
	}

	private void updateLastActivity() {
		synchronized(circuitMap) {
			lastActivity = new Date();
		}
	}

	private long getIdleMilliseconds() {
		synchronized (circuitMap) {
			if(lastActivity == null) {
				return 0;
			}
			final Date now = new Date();
			return now.getTime() - lastActivity.getTime();
		}
	}

	public void removeCircuit(Circuit circuit) {
		synchronized(circuitMap) {
			circuitMap.remove(circuit.getCircuitId());
		}
	}

	public String toString() {
		return "!" + router.getNickname() + "!";
	}

	public void dashboardRender(PrintWriter writer, int flags) throws IOException {
		writer.print("  [Connection router="+ router.getNickname());
		synchronized(circuitMap) {
			writer.print(" circuits="+ circuitMap.size());
		}
		writer.print(" idle="+ (getIdleMilliseconds()/1000) + "s");
		writer.println("]");
	}
}
