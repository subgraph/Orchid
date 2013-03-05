package org.torproject.jtor.directory.impl;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.torproject.jtor.data.HexDigest;
import org.torproject.jtor.data.IPv4Address;
import org.torproject.jtor.directory.DirectoryServer;
import org.torproject.jtor.directory.parsing.DocumentFieldParser;
import org.torproject.jtor.directory.parsing.DocumentParsingHandler;
import org.torproject.jtor.logging.Logger;

/*
 * This class contains the hardcoded 'bootstrap' directory authority
 * server information.
 */
public class TrustedAuthorities {
	private static final String[] DIR_SERVERS = {
		"authority moria1 orport=9101 no-v2 v3ident=D586D18309DED4CD6D57C18FDB97EFA96D330566 128.31.0.39:9131 9695 DFC3 5FFE B861 329B 9F1A B04C 4639 7020 CE31",
		"authority tor26 v1 orport=443 v3ident=14C131DFC5C6F93646BE72FA1401C02A8DF2E8B4 86.59.21.38:80 847B 1F85 0344 D787 6491 A548 92F9 0493 4E4E B85D",
		"authority dizum orport=443 v3ident=E8A9C45EDE6D711294FADF8E7951F4DE6CA56B58 194.109.206.212:80 7EA6 EAD6 FD83 083C 538F 4403 8BBF A077 587D D755",
		"authority Tonga orport=443 bridge no-v2 82.94.251.203:80 4A0C CD2D DC79 9508 3D73 F5D6 6710 0C8A 5831 F16D",
		"authority ides orport=9090 no-v2 v3ident=27B6B5996C426270A5C95488AA5BCEB6BCC86956 216.224.124.114:9030 F397 038A DC51 3361 35E7 B80B D99C A384 4360 292B",
		"authority gabelmoo orport=8080 no-v2 v3ident=ED03BB616EB2F60BEC80151114BB25CEF515B226 80.190.246.100:8180 F204 4413 DAC2 E02E 3D6B CF47 35A1 9BCA 1DE9 7281",
		"authority dannenberg orport=443 no-v2 v3ident=585769C78764D58426B8B52B6651A5A71137189A 213.73.91.31:80 7BE6 83E6 5D48 1413 21C5 ED92 F075 C553 64AC 7123",
		"authority urras orport=80 no-v2 v3ident=80550987E1D626E3EBA5E5E75A458DE0626D088C 208.83.223.34:443 0AD3 FA88 4D18 F89E EA2D 89C0 1937 9E0E 7FD9 4417",
	};

	private List<DirectoryServer> directoryServers = new ArrayList<DirectoryServer>();

	TrustedAuthorities(Logger logger) {
		initialize(logger);
	}

	void initialize(Logger logger) {
		final StringBuilder builder = new StringBuilder();
		for(String entry : DIR_SERVERS) {
			builder.append(entry);
			builder.append('\n');
		}
		final StringReader reader = new StringReader(builder.toString());
		final DocumentFieldParser parser = new DocumentFieldParserImpl(reader, logger);

		parser.setHandler(new DocumentParsingHandler() {
			public void endOfDocument() {}
			public void parseKeywordLine() { processKeywordLine(parser); }
		});
		parser.processDocument();
	}

	private void processKeywordLine(DocumentFieldParser fieldParser) {
		final DirectoryAuthorityStatus status = new DirectoryAuthorityStatus();
		status.setNickname(fieldParser.parseNickname());
		while(fieldParser.argumentsRemaining() > 0)
			processArgument(fieldParser, status);
	}

	private void processArgument(DocumentFieldParser fieldParser, DirectoryAuthorityStatus status) {
		final String item = fieldParser.parseString();
		if(Character.isDigit(item.charAt(0))) {
			parseAddressPort(fieldParser, item, status);
			status.setIdentity(fieldParser.parseFingerprint());
			DirectoryServerImpl server = new DirectoryServerImpl(status);
			if(status.getV3Ident() != null) {
				server.setV3Ident(status.getV3Ident());
			}
			fieldParser.logDebug("Adding trusted authority: " + server);
			directoryServers.add(server);
			return;
		} else {
			parseFlag(fieldParser, item, status);
		}
	}

	private void parseAddressPort(DocumentFieldParser parser, String item, DirectoryAuthorityStatus status) {
		final String[] args = item.split(":");
		status.setAddress(IPv4Address.createFromString(args[0]));
		status.setDirectoryPort(parser.parsePort(args[1]));
	}

	private void parseFlag(DocumentFieldParser parser, String flag, DirectoryAuthorityStatus status) {
		if(flag.equals("v1")) {
			status.setV1Authority();
			status.setHiddenServiceAuthority();
		} else if(flag.equals("hs")) {
			status.setHiddenServiceAuthority();
		} else if(flag.equals("no-hs")) {
			status.unsetHiddenServiceAuthority();
		} else if(flag.equals("bridge")) {
			status.setBridgeAuthority();
		} else if(flag.equals("no-v2")) {
			status.unsetV2Authority();
		} else if(flag.startsWith("orport=")) {
			status.setRouterPort( parser.parsePort(flag.substring(7)));
		} else if(flag.startsWith("v3ident=")) {
			status.setV3Ident(HexDigest.createFromString(flag.substring(8)));
		}
	}

	public List<DirectoryServer> getAuthorityServers() {
		return directoryServers;
	}
	/*
	public DirectoryServer getRandomAuthorityServer() {
		int idx = random.nextInt(directoryServers.size());
		return directoryServers.get(idx);
	}

	public Collection<DirectoryServer> getAuthorityServers() {
		return Collections.unmodifiableCollection(directoryServers);
	}
	*/

}
