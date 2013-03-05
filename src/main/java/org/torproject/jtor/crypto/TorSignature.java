package org.torproject.jtor.crypto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;
import org.torproject.jtor.TorException;
import org.torproject.jtor.TorParsingException;

public class TorSignature {
	private static final String SIGNATURE_BEGIN = "-----BEGIN SIGNATURE-----";
	private static final String ID_SIGNATURE_BEGIN = "-----BEGIN ID SIGNATURE-----";
	private static final String SIGNATURE_END = "-----END SIGNATURE-----";
	private static final String ID_SIGNATURE_END = "-----END ID SIGNATURE-----";

	public static TorSignature createFromPEMBuffer(String buffer) {
		BufferedReader reader = new BufferedReader(new StringReader(buffer));
		final String header = nextLine(reader);
		if(!(SIGNATURE_BEGIN.equals(header) || ID_SIGNATURE_BEGIN.equals(header)))
			throw new TorParsingException("Did not find expected signature BEGIN header");
		return new TorSignature(Base64.decode(parseBase64Data(reader)));
	}
	private static String parseBase64Data(BufferedReader reader) {
		final StringBuilder base64Data = new StringBuilder();
		while(true) {
			final String line = nextLine(reader);
			if(SIGNATURE_END.equals(line) || ID_SIGNATURE_END.equals(line))
				return base64Data.toString();
			base64Data.append(line);
		}
	}
	static String nextLine(BufferedReader reader) {
		try {
			final String line = reader.readLine();
			if(line == null)
				throw new TorParsingException("Did not find expected signature END header");
			return line;
		} catch (IOException e) {
			throw new TorException(e);
		}
	}

	private final byte[] signatureBytes;
	private TorSignature(byte[] signatureBytes) {
		this.signatureBytes = signatureBytes;
	}

	public byte[] getSignatureBytes() {
		return signatureBytes;
	}

	public boolean verify(TorPublicKey publicKey, TorMessageDigest digest) {
		return publicKey.verifySignature(this, digest);
	}
	public String toString() {
		return "TorSignature: (" + signatureBytes.length + " bytes) " + new String(Hex.encode(signatureBytes));
	}



}
