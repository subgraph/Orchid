package org.torproject.jtor.directory.impl;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.torproject.jtor.Logger;
import org.torproject.jtor.TorException;
import org.torproject.jtor.data.HexDigest;
import org.torproject.jtor.directory.Directory;
import org.torproject.jtor.directory.DirectoryServer;
import org.torproject.jtor.directory.KeyCertificate;
import org.torproject.jtor.directory.RouterDescriptor;
import org.torproject.jtor.directory.StatusDocument;


public class DirectoryImpl implements Directory {
	private StatusDocument consensusDocument;	
	private final Logger logger;
	private final Map<HexDigest, KeyCertificate> certificates;
	private final Map<String, RouterDescriptor> routersByNickname;
	private final Map<HexDigest, RouterDescriptor> routersByFingerprint;
	private final List<RouterDescriptor> directoryServerRouters;
	private final TrustedAuthorities trustedAuthorities;
	private final SecureRandom random;

	public DirectoryImpl(Logger logger) {
		this.logger = logger;
		certificates = new HashMap<HexDigest, KeyCertificate>();
		routersByNickname = new HashMap<String, RouterDescriptor>();
		routersByFingerprint = new HashMap<HexDigest, RouterDescriptor>();
		directoryServerRouters = new ArrayList<RouterDescriptor>();
		trustedAuthorities = new TrustedAuthorities(logger);
		random = createRandom();
	}

	private SecureRandom createRandom() {
		try {
			return SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			throw new TorException();
		}
	}

	public Collection<DirectoryServer> getDirectoryAuthorities() {
		return trustedAuthorities.getAuthorityServers();
	}

	public DirectoryServer getRandomDirectoryAuthority() {
		return trustedAuthorities.getRandomAuthorityServer();
	}

	public RouterDescriptor getRandomDirectoryServer() {
		if(directoryServerRouters.isEmpty())
			throw new TorException("No routers loaded");
		int idx = random.nextInt(directoryServerRouters.size());
		return directoryServerRouters.get(idx);
	}
	
	public void addCertificate(KeyCertificate certificate) {
		certificates.put(certificate.getAuthorityFingerprint(), certificate);
	}
	public KeyCertificate findCertificate(HexDigest authorityFingerprint) {
		return certificates.get(authorityFingerprint);
	}

	public void addRouterDescriptor(RouterDescriptor router) {
		if(!router.getNickname().equals("Unnamed"))
			routersByNickname.put(router.getNickname(), router);
		routersByFingerprint.put(router.getFingerprint(), router);
		if(router.getDirectoryPort() != 0) {
			
		}
	}

	public void addConsensusDocument(StatusDocument consensus) {
		this.consensusDocument = consensus;
	}

	public StatusDocument getCurrentConsensusDocument() {
		return consensusDocument;
	}

	public RouterDescriptor getRouterByName(String name) {
		if(name.equals("Unnamed"))
			return null;
		return routersByNickname.get(name);
	}

	public List<RouterDescriptor> getRouterListByNames(List<String> names) {
		final List<RouterDescriptor> routers = new ArrayList<RouterDescriptor>();
		for(String n: names) {
			final RouterDescriptor r = getRouterByName(n);
			if(r == null)
				throw new TorException("Could not find router named: "+ n);
			routers.add(r);
		}
		return routers;
	}

	Logger getLogger() {
		return logger;
	}
	
}
