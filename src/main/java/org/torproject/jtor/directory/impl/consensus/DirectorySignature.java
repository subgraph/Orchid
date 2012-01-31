package org.torproject.jtor.directory.impl.consensus;

import org.torproject.jtor.crypto.TorSignature;
import org.torproject.jtor.data.HexDigest;

public class DirectorySignature {
	private final HexDigest identityDigest;
	private final HexDigest signingKeyDigest;
	private final TorSignature signature;
	
	DirectorySignature(HexDigest identityDigest, HexDigest signingKeyDigest, TorSignature signature) {
		this.identityDigest = identityDigest;
		this.signingKeyDigest = signingKeyDigest;
		this.signature = signature;
	}
	
	public HexDigest getIdentityDigest() {
		return identityDigest;
	}
	
	public HexDigest getSigningKeyDigest() {
		return signingKeyDigest;
	}
	
	public TorSignature getSignature() {
		return signature;
	}

}
