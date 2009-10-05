package org.torproject.jtor.directory;

import java.util.Set;

import org.torproject.jtor.data.Timestamp;

public interface StatusDocument {
	
	Timestamp getValidAfterTime();
	Timestamp getFreshUntilTime();
	Timestamp getValidUntilTime();
	int getConsensusMethod();
	int getVoteSeconds();
	int getDistSeconds();
	Set<String> getClientVersions();
	Set<String> getServerVersions();
	boolean isLive();
	boolean isConsensusDocument();

}
