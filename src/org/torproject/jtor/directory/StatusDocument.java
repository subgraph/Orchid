package org.torproject.jtor.directory;

import java.util.List;
import java.util.Set;

import org.torproject.jtor.data.Timestamp;

public interface StatusDocument extends Document {
	
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
	List<RouterStatus> getRouterStatusEntries();

}
