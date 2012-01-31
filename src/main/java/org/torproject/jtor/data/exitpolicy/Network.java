package org.torproject.jtor.data.exitpolicy;

import org.torproject.jtor.TorParsingException;
import org.torproject.jtor.data.IPv4Address;

public class Network {
	public static final Network ALL_ADDRESSES = new Network(IPv4Address.createFromString("0.0.0.0"), 0);
	public static Network createFromString(String networkString) {
		final String[] parts = networkString.split("/");
		final IPv4Address network = IPv4Address.createFromString(parts[0]);
		if(parts.length == 1)
			return new Network(network, 32);
		
		if(parts.length != 2)
			throw new TorParsingException("Invalid network CIDR notation: " + networkString);

		try {
			final int maskBits = Integer.parseInt(parts[1]);
			return new Network(network, maskBits);
		} catch(NumberFormatException e) {
			throw new TorParsingException("Invalid netblock mask bit value: " + parts[1]);
		}
	}
	
	private final IPv4Address network;
	private final int maskValue;
	
	Network(IPv4Address network, int bits) {
		this.network = network;
		this.maskValue = createMask(bits);
	}
	
	private static int createMask(int maskBits) {
		return maskBits == 0 ? 0 : (1 << 31) >> (maskBits - 1);
	}
	
	public boolean contains(IPv4Address address) {
		return (address.getAddressData() & maskValue) == (network.getAddressData() & maskValue);
	}

}
