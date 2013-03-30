package org.torproject.jtor.directory;

import java.util.Date;

public interface GuardEntry {
	void setDownSince(Date date);
	void setLastConnectAttempt(Date date);
	void clearDownSince();
	void setUnlistedSince(Date date);
	void clearUnlistedSince();
	String getNickname();
	String getIdentity();
	String getVersion();
	Date getCreatedTime();
	Date getDownSince();
	Date getLastConnectAttempt();
	Date getUnlistedSince();
}
