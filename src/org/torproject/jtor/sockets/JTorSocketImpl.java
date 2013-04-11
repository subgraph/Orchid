package org.torproject.jtor.sockets;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.net.SocketOptions;
import java.net.SocketTimeoutException;

import org.torproject.jtor.TorClient;
import org.torproject.jtor.circuits.OpenStreamResponse;
import org.torproject.jtor.circuits.Stream;

public class JTorSocketImpl extends SocketImpl {
	
	private final TorClient torClient;
	private final Object streamLock = new Object();	

	private Stream stream;

	JTorSocketImpl(TorClient torClient) {
		this.torClient = torClient;
	}

	public void setOption(int optID, Object value) throws SocketException {
		// don't throw exception here, this is required for original socket
	}

	public Object getOption(int optID) throws SocketException {
		if(optID == SocketOptions.SO_LINGER) {
			return 0;
		}
		throw new UnsupportedOperationException();
	}

	@Override
	protected void create(boolean stream) throws IOException {
		
	}

	@Override
	protected void connect(String host, int port) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void connect(InetAddress address, int port) throws IOException {
		throw new UnsupportedOperationException();
		
	}

	@Override
	protected void connect(SocketAddress address, int timeout)
			throws IOException {
		if(!(address instanceof InetSocketAddress)) {
			throw new IllegalArgumentException("Unsupported address type");
		}
		final InetSocketAddress inetAddress = (InetSocketAddress) address;
		
		doConnect(addressToName(inetAddress), inetAddress.getPort());
	}
	
	private String addressToName(InetSocketAddress address) {
		if(address.getAddress() != null) {
			return address.getAddress().getHostAddress();
		} else {
			return address.getHostName();
		}
	}

	private void doConnect(String host, int port) throws IOException {
		synchronized(streamLock) {
			if(stream != null) {
				throw new SocketException("Already connected");
			}
			stream = openExitStream(host, port);
		}
	}
	
	private Stream openExitStream(String host, int port) throws IOException {
		try {
			final OpenStreamResponse osr = torClient.openExitStreamTo(host, port);
			switch(osr.getStatus()) {
			case STATUS_STREAM_ERROR:
				throw new SocketException("Connection failed to "+host+ ":"+ port +"["+ osr.getErrorCodeMessage() + "]");
			case STATUS_STREAM_TIMEOUT:
				throw new SocketTimeoutException("Timeout connecting to "+host +":"+port);
			case STATUS_STREAM_OPENED:
				return osr.getStream();
			}
		} catch (InterruptedException e) {
			throw new InterruptedIOException("Connection interrupted to "+host +":"+ port);
		}
		return null;
	}

	@Override
	protected void bind(InetAddress host, int port) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void listen(int backlog) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void accept(SocketImpl s) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected InputStream getInputStream() throws IOException {
		synchronized (streamLock) {
			if(stream == null) {
				throw new IOException("Not connected");
			}
			return stream.getInputStream();
		}
	}

	@Override
	protected OutputStream getOutputStream() throws IOException {
		synchronized (streamLock) {
			if(stream == null) {
				throw new IOException("Not connected");
			}
			return stream.getOutputStream();
		}
	}

	@Override
	protected int available() throws IOException {
		synchronized(streamLock) {
			if(stream == null) {
				throw new IOException("Not connected");
			}
			return stream.getInputStream().available();
		}
	}

	@Override
	protected void close() throws IOException {
		synchronized (streamLock) {
			stream.close();
			stream = null;
		}
	}

	@Override
	protected void sendUrgentData(int data) throws IOException {
		throw new UnsupportedOperationException();
	}
	
	 protected void shutdownInput() throws IOException {
      //throw new IOException("Method not implemented!");
    }
	 
	 protected void shutdownOutput() throws IOException {
      //throw new IOException("Method not implemented!");
    }
}
