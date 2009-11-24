package org.torproject.jtor.directory;

import org.torproject.jtor.crypto.TorPublicKey;
import org.torproject.jtor.data.HexDigest;
import org.torproject.jtor.data.IPv4Address;

public interface Router {

	String getNickname();
	IPv4Address getAddress();
	int getOnionPort();
	int getDirectoryPort();
	TorPublicKey getIdentityKey();
	HexDigest getIdentityHash();
	RouterDescriptor getCurrentDescriptor();
	HexDigest getDescriptorDigest();
	TorPublicKey getOnionKey();
}
