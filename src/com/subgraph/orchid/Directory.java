package com.subgraph.orchid;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.subgraph.orchid.data.HexDigest;
import com.subgraph.orchid.events.EventHandler;

/**
 * 
 * Main interface for accessing directory information and interacting
 * with directory authorities and caches.
 *
 */
public interface Directory {
	boolean haveMinimumRouterInfo();
	void loadFromStore();
	void waitUntilLoaded();
	void storeCertificates();
	void storeConsensus();
	void storeDescriptors();
	Collection<DirectoryServer> getDirectoryAuthorities();
	DirectoryServer getRandomDirectoryAuthority();
	void addCertificate(KeyCertificate certificate);
	Set<HexDigest> getRequiredCertificates();
	void addRouterDescriptor(RouterDescriptor router);
	void addConsensusDocument(ConsensusDocument consensus);
	ConsensusDocument getCurrentConsensusDocument();
	void registerConsensusChangedHandler(EventHandler handler);
	void unregisterConsensusChangedHandler(EventHandler handler);
	KeyCertificate findCertificate(HexDigest authorityFingerprint);
	Router getRouterByName(String name);
	Router getRouterByIdentity(HexDigest identity);
	List<Router> getRouterListByNames(List<String> names);
	List<Router> getRoutersWithDownloadableDescriptors();
	List<Router> getAllRouters();
	void markDescriptorInvalid(RouterDescriptor descriptor);
	
	GuardEntry createGuardEntryFor(Router router);
	List<GuardEntry> getGuardEntries();
	void removeGuardEntry(GuardEntry entry);
	void addGuardEntry(GuardEntry entry);
}
