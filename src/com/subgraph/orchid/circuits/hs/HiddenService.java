package com.subgraph.orchid.circuits.hs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.subgraph.orchid.crypto.TorMessageDigest;
import com.subgraph.orchid.data.Base32;
import com.subgraph.orchid.data.HexDigest;

public class HiddenService {
	
	private final byte[] permanentId;
	private final byte[] cookie;

	private HSDescriptor descriptor;
	private RendezvousCircuit circuit;
	
	private static byte[] decodeOnion(String onionAddress) {
		final int idx = onionAddress.indexOf(".onion");
		if(idx == -1) {
			return Base32.base32Decode(onionAddress);
		} else {
			return Base32.base32Decode(onionAddress.substring(0, idx));
		}
	}
	
	HiddenService(String onionAddress) {
		this(decodeOnion(onionAddress), null);
	}
	
	HiddenService(byte[] permanentId, byte[] cookie) {
		this.permanentId = permanentId;
		this.cookie = cookie;
	}

	boolean hasCurrentDescriptor() {
		return false;
	}
	
	HSDescriptor getDescriptor() {
		return descriptor;
	}

	void setDescriptor(HSDescriptor descriptor) {
		this.descriptor = descriptor;
	}

	RendezvousCircuit getCircuit() {
		return circuit;
	}
	
	void setCircuit(RendezvousCircuit circuit) {
		this.circuit = circuit;
	}

	List<HexDigest> getAllCurrentDescriptorIds() {
		final List<HexDigest> ids = new ArrayList<HexDigest>();
		ids.add(getCurrentDescriptorId(0));
		ids.add(getCurrentDescriptorId(1));
		return ids;
	}

	HexDigest getCurrentDescriptorId(int replica) {
		final TorMessageDigest digest = new TorMessageDigest();
		digest.update(permanentId);
		digest.update(getCurrentSecretId(replica));
		return digest.getHexDigest();
	}

	byte[] getCurrentSecretId(int replica) {
		final TorMessageDigest digest = new TorMessageDigest();
		digest.update(getCurrentTimePeriod());
		if(cookie != null && cookie.length != 0) {
			digest.update(cookie);
		}
		digest.update(new byte[] { (byte) replica });
		return digest.getDigestBytes();
	}

	byte[] getCurrentTimePeriod() {
		final long now = System.currentTimeMillis() / 1000;
		final int idByte = permanentId[0] & 0xFF;
		return calculateTimePeriod(now, idByte);
	}

	static byte[] calculateTimePeriod(long currentTime, int idByte) {
		final long t = (currentTime + (idByte * 86400L / 256)) / 86400L;
		return toNetworkBytes(t);
	}
	
	static byte[] toNetworkBytes(long value) {
		final byte[] result = new byte[4];
		for(int i = 3; i >= 0; i--) {
			result[i] = (byte) (value & 0xFF);
			value >>= 8;
		}
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(permanentId);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HiddenService other = (HiddenService) obj;
		if (!Arrays.equals(permanentId, other.permanentId))
			return false;
		return true;
	}
}
