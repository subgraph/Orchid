package org.torproject.jtor.directory.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.InflaterInputStream;

import org.torproject.jtor.TorException;

public class HttpConnection {
	private final static String HTTP_RESPONSE_REGEX = "HTTP/1\\.(\\d) (\\d+) (.*)";
	private final static String CONTENT_LENGTH_HEADER = "Content-Length";
	private final static String CONTENT_ENCODING_HEADER = "Content-Encoding";
	private final Date ifModifiedSince = null;
	private final String host;
	private final BufferedReader reader;
	private final BufferedWriter writer;
	private final Map<String, String> headers;
	private int responseCode;
	private boolean bodyCompressed;
	private String responseMessage;
	private Reader bodyReader;
	
	HttpConnection(String host, BufferedReader reader, BufferedWriter writer) {
		this.host = host;
		this.reader = reader;
		this.writer = writer;
		this.headers = new HashMap<String, String>();
	}

	void sendGetRequest(String request) {
		final StringBuilder sb = new StringBuilder();
		sb.append("GET ");
		sb.append(request);
		sb.append(" HTTP/1.0\r\n");
		sb.append("Host: "+ host +"\r\n");
		if(ifModifiedSince != null) {
			
		}
		sb.append("\r\n");
		
		try {
			writer.write(sb.toString());
			writer.flush();
		} catch (IOException e) {
			throw new TorException("IO exception sending GET request", e);
		}
	}
	
	void readResponse() {
		readStatusLine();
		readHeaders();
		readBody();
	}
	
	int getStatusCode() {
		return responseCode;
	}
	
	String getStatusMessage() {
		return responseMessage;
	}
	Reader getBodyReader() {
		return bodyReader;
	}
	
	private void readStatusLine() {
		final String line = nextResponseLine();	
		final Pattern p = Pattern.compile(HTTP_RESPONSE_REGEX);
		final Matcher m = p.matcher(line);
		if(!m.find() || m.groupCount() != 3) 
			throw new TorException("Error parsing HTTP response line: "+ line);
		
		try {
			int n1 = Integer.parseInt(m.group(1));
			int n2 = Integer.parseInt(m.group(2));
			if( (n1 != 0 && n1 != 1) ||
					(n2 < 100 || n2 >= 600))
				throw new TorException("Failed to parse header: "+ line);
			responseCode = n2;
			responseMessage = m.group(3);
		} catch(NumberFormatException e) {
			throw new TorException("Failed to parse header: "+ line);
		}
	}
	
	private void readHeaders() {
		headers.clear();
		while(true) {
			final String line = nextResponseLine();
			if(line.length() == 0)
				return;
			final String[] args = line.split(": ", 2);
			if(args.length != 2)
				throw new TorException("Failed to parse HTTP header: "+ line);
			headers.put(args[0], args[1]);
		}
	}
	
	private String nextResponseLine() {
		try {
			final String line = reader.readLine();
			if(line == null)
				throw new TorException("Unexpected EOF reading HTTP response");
			return line;
		} catch (IOException e) {
			throw new TorException("IO error reading HTTP response", e);
		}
	}
	
	private void readBody() {
		processContentEncodingHeader();
		
		if(headers.containsKey(CONTENT_LENGTH_HEADER)) 
			readBodyFromContentLength();
		else 
			readBodyUntilEOF();
	}
	
	private void processContentEncodingHeader() {
		final String encoding = headers.get(CONTENT_ENCODING_HEADER);
		if(encoding == null || encoding.equals("identity")) 
			bodyCompressed = false;
		else if(encoding.equals("deflate") || encoding.equals("x-deflate"))
			bodyCompressed = true;
		else
			throw new TorException("Unrecognized content encoding: "+ encoding);
	}
	
	private void readBodyFromContentLength() {
		int bodyLength = Integer.parseInt(headers.get(CONTENT_LENGTH_HEADER));
		char[] bodyBuffer = new char[bodyLength];
		readAll(bodyBuffer);
		
		bodyReader = charBufferToReader(bodyBuffer, bodyLength);
	}
	
	private void readBodyUntilEOF() {
		char[] bodyBuffer = new char[1024];
		int offset = 0;
		while(true) {
			try {
				int n = reader.read(bodyBuffer, offset, bodyBuffer.length - offset);
				if(n == -1) {
					bodyReader = charBufferToReader(bodyBuffer, offset);
					return;
				}
				offset += n;
				if(offset == bodyBuffer.length) 
					bodyBuffer = expandCharBuffer(bodyBuffer);
					
			} catch (IOException e) {
				throw new TorException("IO error reading HTTP response");
			}		
		}
	}
	
	private char[] expandCharBuffer(char[] buffer) {
		final int newLength = buffer.length * 2;
		final char[] newBuffer = new char[newLength];
		System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
		return newBuffer;
	}
	
	private Reader charBufferToReader(char[] buffer, int length) {
		if(bodyCompressed) {
			final byte[] bytes = new byte[length];
			for(int i = 0; i < length; i++)
				bytes[i] = (byte) buffer[i];
			final ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
			final InflaterInputStream inflater = new InflaterInputStream(byteStream);
			return createInputStreamReader(inflater);
		} else {
			return new StringReader(new String(buffer, 0, length));
		}
	}
	
	private Reader createInputStreamReader(InputStream input) {
		try {
			return new InputStreamReader(input, "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			throw new TorException("Unsupported character set encoding: "+ e.getMessage());
		}
	}

	private void readAll(char[] buffer) {
		int offset = 0;
		int remaining = buffer.length;
		while(remaining > 0) {
			try {
				int n = reader.read(buffer, offset, remaining);
				if(n == -1)
					throw new TorException("Unexpected EOF reading response");
				offset += n;
				remaining -= n;
			} catch (IOException e) {
				throw new TorException("IO error reading HTTP response");
			}
		}
	}
}
