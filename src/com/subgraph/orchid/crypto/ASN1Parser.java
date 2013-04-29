package com.subgraph.orchid.crypto;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ASN1Parser {
	
	private final static int ASN1_TAG_SEQUENCE = 16;
	private final static int ASN1_TAG_INTEGER = 2;
	private final static int ASN1_TAG_BITSTRING = 3;
	
	static interface ASN1Object {};
	
	static class ASN1Sequence implements ASN1Object {
		private final List<ASN1Object> items;
		
		ASN1Sequence(List<ASN1Object> items) {
			this.items = items;
		}
		
		List<ASN1Object> getItems() {
			return items;
		}
	}
	
	static class ASN1Integer implements ASN1Object {
		final BigInteger value;
		ASN1Integer(BigInteger value) {
			this.value = value;
		}
		BigInteger getValue() {
			return value;
		}
	}

	static class ASN1BitString implements ASN1Object {
		final byte[] bytes;
		
		ASN1BitString(byte[] bytes) {
			this.bytes = bytes;
		}
		
		byte[] getBytes() {
			return bytes;
		}
	}

	static class ASN1Blob extends ASN1BitString {
		ASN1Blob(byte[] bytes) {
			super(bytes);
		}
	}

	ASN1Object parseASN1(ByteBuffer data) {
		final int typeOctet = data.get() & 0xFF;
		//final int classBits = (typeOctet >> 6) & 0x03;
		//final boolean isConstructed = ((typeOctet >> 5) & 0x01) == 1;
		final int tag = typeOctet & 0x1F;
		final int length = parseASN1Length(data);
		
		final ByteBuffer newBuffer = data.slice();
		newBuffer.limit(length);
		data.position(data.position() + length);
		
		switch(tag) {
		case ASN1_TAG_SEQUENCE:
			return parseASN1Sequence(newBuffer);
		case ASN1_TAG_INTEGER:
			return parseASN1Integer(newBuffer);
		case ASN1_TAG_BITSTRING:
			return parseASN1BitString(newBuffer);
		default:
			return createBlob(newBuffer);
		}
		
	}
	private int parseASN1Length(ByteBuffer data) {
		final int firstOctet = data.get() & 0xFF;
		if(firstOctet < 0x80) {
			return firstOctet;
		}
		return parseASN1LengthLong(firstOctet & 0x7F, data);
	}
	
	private int parseASN1LengthLong(int lengthOctets, ByteBuffer data) {
		if(lengthOctets == 0 || lengthOctets > 3) {
			// indefinite form or too long
			throw new IllegalArgumentException();
		}
		int length = 0;
		for(int i = 0; i < lengthOctets; i++) {
			length <<= 8;
			length |= (data.get() & 0xFF);
		}
		return length;
	}
	
	private ASN1Sequence parseASN1Sequence(ByteBuffer data) {
		final List<ASN1Object> obs = new ArrayList<ASN1Object>();
		while(data.hasRemaining()) {
			obs.add(parseASN1(data));
		}
		return new ASN1Sequence(obs);
	}
	
	private ASN1Integer parseASN1Integer(ByteBuffer data) {
		byte[] bs = new byte[data.remaining()];
		data.get(bs);
		return new ASN1Integer(new BigInteger(bs));
	}
	
	private ASN1BitString parseASN1BitString(ByteBuffer data) {
		final int unusedBits = data.get() & 0xFF;
		if(unusedBits != 0) {
			throw new IllegalArgumentException();
		}
		final byte[] bs = new byte[data.remaining()];
		data.get(bs);
		return new ASN1BitString(bs);
	}

	private ASN1Blob createBlob(ByteBuffer data) {
		final byte[] bs = new byte[data.remaining()];
		data.get(bs);
		return new ASN1Blob(bs);
	}
}
