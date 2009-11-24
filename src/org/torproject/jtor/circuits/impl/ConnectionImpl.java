package org.torproject.jtor.circuits.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.net.ssl.SSLSocket;

import org.torproject.jtor.TorException;
import org.torproject.jtor.circuits.Connection;
import org.torproject.jtor.circuits.ConnectionClosedException;
import org.torproject.jtor.circuits.ConnectionConnectException;
import org.torproject.jtor.circuits.cells.Cell;
import org.torproject.jtor.directory.Router;

/**
 * This class represents a transport link between two onion routers or
 * between an onion proxy and an entry router.
 *
 */
public class ConnectionImpl implements Connection {
	
	private final static int DEFAULT_CONNECT_TIMEOUT = 30000;

	private final SSLSocket socket;
	private final ConnectionManagerImpl manager;
	private InputStream input;
	private OutputStream output;
	private final Router router;
	private final Map<Integer, CircuitImpl> circuitMap;
	private final BlockingQueue<Cell> connectionControlCells;
	private int currentId = 1;
	private boolean isConnected;
	private final Thread readCellsThread;
	
	ConnectionImpl(ConnectionManagerImpl manager, SSLSocket socket, Router router) {
		this.manager = manager;
		this.socket = socket;	
		this.router = router;
		this.circuitMap = new HashMap<Integer, CircuitImpl>();
		this.readCellsThread = new Thread(createReadCellsRunnable());
		this.readCellsThread.setDaemon(true);
		this.connectionControlCells = new LinkedBlockingQueue<Cell>();
	}
	
	public Router getRouter() {
		return router;
	}

	int allocateCircuitId(CircuitImpl circuit) {
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
	
	public void connect() {
		try {
			doConnect();
		} catch (IOException e) {
			throw new ConnectionConnectException(e.getMessage());
		} catch (InterruptedException e) {
			throw new ConnectionConnectException("Handshake interrupted");
		}
		manager.addActiveConnection(this);
		isConnected = true;
	}
	
	private void doConnect() throws IOException, InterruptedException {
		socket.connect(routerToSocketAddress(router), DEFAULT_CONNECT_TIMEOUT);
		input = socket.getInputStream();
		output = socket.getOutputStream();
		readCellsThread.start();
		final ConnectionHandshakeV2 handshake = new ConnectionHandshakeV2(this, socket);
		
		handshake.runHandshake();
	}
	
	
	private SocketAddress routerToSocketAddress(Router router) {
		final InetAddress address = router.getAddress().toInetAddress();
		return new InetSocketAddress(address, router.getOnionPort());
	}
	
	
	public void sendCell(Cell cell)  {
		try {
			output.write(cell.getCellBytes());
		} catch (IOException e) {
			closeSocket();
			manager.removeActiveConnection(this);
			throw new ConnectionClosedException();
		}
	}
	
	private Cell recvCell() {
		try {
			return CellImpl.readFromInputStream(input);
		} catch (IOException e) {
			closeSocket();
			manager.removeActiveConnection(this);
			throw new ConnectionClosedException();
		}
	}
	
	private void closeSocket() {
		try {
			socket.close();
			isConnected = false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Runnable createReadCellsRunnable() {
		return new Runnable() {
			public void run() {
				readCellsLoop();				
			}
		};
	}
	private void readCellsLoop() {
		while(!Thread.interrupted()) {
			try {
				processCell( recvCell() );
			} catch(ConnectionClosedException e) {
				notifyCircuitsLinkClosed();
				return;
			} catch(TorException e) {
				System.out.println("Unhandled tor exception");
				e.printStackTrace();
			}
		}
	}
	
	private void notifyCircuitsLinkClosed() {
		
	}
	
	Cell readConnectionControlCell() {
		try {
			return connectionControlCells.take();
		} catch (InterruptedException e) {
			closeSocket();
			throw new ConnectionClosedException();
		}
	}
	private void processCell(Cell cell) {
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
			processControlCell(cell);
			break;
		case Cell.DESTROY:
			System.out.println("Got destroy cell: "+ cell.getByte());
		default:
			// Ignore everything else
			break;
		}
	}
	
	private void processRelayCell(Cell cell) {
		
		final CircuitImpl circuit = circuitMap.get(cell.getCircuitId());
		if(circuit == null) {
			System.out.println("NO CIRCUIT");
			// XXX no circuit for relay cell
			return;
		}
		circuit.deliverRelayCell(cell);
	}
	
	private void processControlCell(Cell cell) {
		final CircuitImpl circuit = circuitMap.get(cell.getCircuitId());
		if(circuit == null) {
			System.out.println("NO CIRCUIT");
			// XXX no circuit found
			return;
		}
		circuit.deliverControlCell(cell);
	}
}
