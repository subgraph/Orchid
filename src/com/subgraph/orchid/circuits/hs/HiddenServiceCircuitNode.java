package com.subgraph.orchid.circuits.hs;

import com.subgraph.orchid.circuits.CircuitNodeImpl;

public class HiddenServiceCircuitNode extends CircuitNodeImpl {

	HiddenServiceCircuitNode(CircuitNodeImpl previous) {
		super(null, previous);
	}

	@Override
	public String toString() {
		return "|(Hidden Service)|";
	}
	
	byte[] getPublicKeyBytes() {
		return getKeyAgreement().getPublicKeyBytes();
	}
	
	

}
