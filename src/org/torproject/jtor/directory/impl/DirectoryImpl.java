package org.torproject.jtor.directory.impl;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.torproject.jtor.Logger;
import org.torproject.jtor.TorConfig;
import org.torproject.jtor.TorException;
import org.torproject.jtor.data.HexDigest;
import org.torproject.jtor.data.RandomSet;
import org.torproject.jtor.directory.Directory;
import org.torproject.jtor.directory.DirectoryServer;
import org.torproject.jtor.directory.DirectoryStore;
import org.torproject.jtor.directory.KeyCertificate;
import org.torproject.jtor.directory.Router;
import org.torproject.jtor.directory.RouterDescriptor;
import org.torproject.jtor.directory.RouterStatus;
import org.torproject.jtor.directory.StatusDocument;

public class DirectoryImpl implements Directory {
	private final DirectoryStore store;
	private final Logger logger;
	private final Map<HexDigest, KeyCertificate> certificates;
	private final Map<HexDigest, RouterImpl> routersByIdentity;
	private final Map<String, RouterImpl> routersByNickname;
	private final RandomSet<RouterImpl> directoryCaches;
	private List<DirectoryServer> directoryAuthorities;

	private final SecureRandom random;
	private StatusDocument currentConsensus;
	private boolean descriptorsDirty;

	public DirectoryImpl(Logger logger, TorConfig config) {
		this.logger = logger;
		store = new DirectoryStoreImpl(logger, config);
		certificates = new HashMap<HexDigest, KeyCertificate>();
		routersByIdentity = new HashMap<HexDigest, RouterImpl>();
		routersByNickname = new HashMap<String, RouterImpl>();
		directoryCaches = new RandomSet<RouterImpl>();
		random = createRandom();
		loadAuthorityServers();
	}

	private SecureRandom createRandom() {
		try {
			return SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			throw new TorException();
		}
	}

	private void loadAuthorityServers() {
		final TrustedAuthorities trusted = new TrustedAuthorities(logger);
		directoryAuthorities = trusted.getAuthorityServers();
	}
	
	public void loadFromStore() {
		store.loadCertificates(this);
		store.loadConsensus(this);
		store.loadRouterDescriptors(this);
	}
	
	public Collection<DirectoryServer> getDirectoryAuthorities() {
		return directoryAuthorities;
	}

	public DirectoryServer getRandomDirectoryAuthority() {
		final int idx = random.nextInt(directoryAuthorities.size());
		return directoryAuthorities.get(idx);
	}

	public Router getRandomDirectoryServer() {
		if(directoryCaches.isEmpty())
			return getRandomDirectoryAuthority();
		return directoryCaches.getRandomElement();
	}
	
	public void addCertificate(KeyCertificate certificate) {
		certificates.put(certificate.getAuthorityFingerprint(), certificate);
	}
	
	public KeyCertificate findCertificate(HexDigest authorityFingerprint) {
		return certificates.get(authorityFingerprint);
	}
	
	public void storeCertificates() {
		final List<KeyCertificate> certs = new ArrayList<KeyCertificate>(); 
		for(KeyCertificate c: certificates.values()) 
			certs.add(c);
		store.saveCertificates(certs);
	}

	public void addRouterDescriptor(RouterDescriptor router) {
		addDescriptor(router);
	}
	
	public void storeConsensus() {
		if(currentConsensus != null)
			store.saveConsensus(currentConsensus);
	}
	
	public synchronized void storeDescriptors() {
		if(!descriptorsDirty)
			return;
		final List<RouterDescriptor> descriptors = new ArrayList<RouterDescriptor>();
		for(Router router: routersByIdentity.values()) {
			final RouterDescriptor descriptor = router.getCurrentDescriptor();
			if(descriptor != null)
				descriptors.add(descriptor);
		}
		store.saveRouterDescriptors(descriptors);
		descriptorsDirty = false;
	}
	
	public void addConsensusDocument(StatusDocument consensus) {
		if(consensus.equals(currentConsensus))
			return;
		
		if(currentConsensus != null && consensus.getValidAfterTime().isBefore(currentConsensus.getValidAfterTime())) {
			logger.warn("New consensus document is older than current consensus document");
			return;
		}
		
		final Map<HexDigest, RouterImpl> oldRouterByIdentity = new HashMap<HexDigest, RouterImpl>(routersByIdentity);
		clearAll();
		
		for(RouterStatus status: consensus.getRouterStatusEntries()) {
			if(status.hasFlag("Running") && status.hasFlag("Valid")) {
				final RouterImpl router = updateOrCreateRouter(status, oldRouterByIdentity);
				addRouter(router);
				classifyRouter(router);
			}
		}
		logger.debug("Loaded "+ routersByIdentity.size() +" routers from consensus document");
		currentConsensus = consensus;
		store.saveConsensus(consensus);
	}
	private RouterImpl updateOrCreateRouter(RouterStatus status, Map<HexDigest, RouterImpl> knownRouters) {
		final RouterImpl router = knownRouters.get(status.getIdentity());
		if(router == null)
			return RouterImpl.createFromRouterStatus(status);
		router.updateStatus(status);
		return router;
	}
	
	private void clearAll() {
		routersByIdentity.clear();
		routersByNickname.clear();
		directoryCaches.clear();
	}
	
	private void classifyRouter(RouterImpl router) {
		if(isValidDirectoryCache(router)) 
			directoryCaches.add(router);
		else
			directoryCaches.remove(router);
	}
	
	private boolean isValidDirectoryCache(RouterImpl router) {
		if(router.getDirectoryPort() == 0)
			return false;
		if(router.hasFlag("BadDirectory"))
			return false;
		return router.hasFlag("V2Dir");
	}
	
	private void addRouter(RouterImpl router) {
		routersByIdentity.put(router.getIdentityHash(), router);
		addRouterByNickname(router);
		if(router.getDirectoryPort() != 0)
			directoryCaches.add(router);
	}
	
	private void addRouterByNickname(RouterImpl router) {
		final String name = router.getNickname();
		if(name == null || name.equals("Unnamed"))
			return;
		if(routersByNickname.containsKey(router.getNickname())) {
			logger.warn("Duplicate router nickname: "+ router.getNickname());
			return;
		}
		routersByNickname.put(name, router);
	}
	
	synchronized void addDescriptor(RouterDescriptor descriptor) {
		final HexDigest identity = descriptor.getIdentityKey().getFingerprint();
		if(!routersByIdentity.containsKey(identity)) {
			logger.warn("Could not find router for descriptor: "+ descriptor.getIdentityKey().getFingerprint());
			return;
		}
		final RouterImpl router = routersByIdentity.get(identity);
		final RouterDescriptor oldDescriptor = router.getCurrentDescriptor();
		if(descriptor.equals(oldDescriptor))
			return;
		
		if(oldDescriptor != null && oldDescriptor.isNewerThan(descriptor)) {
			logger.warn("Attempting to add descriptor to router which is older than the descriptor we already have");
			return;
		}
		descriptorsDirty = true;
		router.updateDescriptor(descriptor);
		classifyRouter(router);
	}
	
	synchronized public List<Router> getRoutersWithDownloadableDescriptors() {
		final List<Router> routers = new ArrayList<Router>();
		for(RouterImpl router: routersByIdentity.values()) {
			if(router.isDescriptorDownloadable())
				routers.add(router);
		}
		
		for(int i = 0; i < routers.size(); i++) {
			final Router a = routers.get(i);
			final int swapIdx = random.nextInt(routers.size());
			final Router b = routers.get(swapIdx);
			routers.set(i, b);
			routers.set(swapIdx, a);
		}
		
		return routers;
	}
	
	synchronized public void markDescriptorInvalid(RouterDescriptor descriptor) {
		removeRouterByIdentity(descriptor.getIdentityKey().getFingerprint());	
	}
	
	private void removeRouterByIdentity(HexDigest identity) {
		logger.debug("Removing: "+ identity);
		final RouterImpl router = routersByIdentity.remove(identity);
		if(router == null)
			return;
		final RouterImpl routerByName = routersByNickname.get(router.getNickname());
		if(routerByName.equals(router))
			routersByNickname.remove(router.getNickname());
		directoryCaches.remove(router);
	}

	public StatusDocument getCurrentConsensusDocument() {
		return currentConsensus;
	}

	public Router getRouterByName(String name) {
		if(name.equals("Unnamed"))
			return null;
		return routersByNickname.get(name);
	}

	public List<Router> getRouterListByNames(List<String> names) {
		final List<Router> routers = new ArrayList<Router>();
		for(String n: names) {
			final Router r = getRouterByName(n);
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
