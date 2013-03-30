package org.torproject.jtor.directory.impl;

import java.util.Date;

import org.torproject.jtor.directory.GuardEntry;

public class GuardEntryImpl implements GuardEntry {
	private final static String NL = System.getProperty("line.separator");
	
	private final StateFile stateFile;
	private final String nickname;
	private final String identity;
	private String version;
	private Date createdTime;
	
	private boolean isAdded;
	private Date unlistedSince;
	private Date downSince;
	private Date lastConnect;
	
	GuardEntryImpl(StateFile stateFile, String nickname, String identity) {
		this.stateFile = stateFile;
		this.nickname = nickname;
		this.identity = identity;
	}

	void setAddedFlag() {
		isAdded = true;
	}
	
	void setVersion(String version) {
		this.version = version;
	}
	
	void setCreatedTime(Date date) {
		this.createdTime = date;
	}

	public void setDownSince(Date date) {
		downSince = date;
		if(isAdded) {
			stateFile.writeFile();
		}
	}

	public void setLastConnectAttempt(Date date) {
		lastConnect = date;
		if(isAdded) {
			stateFile.writeFile();
		}
	}

	public void clearDownSince() {
		downSince = null;
		lastConnect = null;
		if(isAdded) {
			stateFile.writeFile();
		}
	}

	public void setUnlistedSince(Date date) {
		unlistedSince = date;
		if(isAdded) {
			stateFile.writeFile();
		}
	}

	public void clearUnlistedSince() {
		unlistedSince = null;
		if(isAdded) {
			stateFile.writeFile();
		}
	}

	public String getNickname() {
		return nickname;
	}

	public String getIdentity() {
		return identity;
	}

	public String getVersion() {
		return version;
	}

	public Date getCreatedTime() {
		return createdTime;
	}

	public Date getDownSince() {
		return downSince;
	}

	public Date getLastConnectAttempt() {
		return lastConnect;
	}

	public Date getUnlistedSince() {
		return unlistedSince;
	}
	
	public String writeToString() {
		final StringBuilder sb = new StringBuilder();
		appendEntryGuardLine(sb);
		appendEntryGuardAddedBy(sb);
		if(downSince != null) {
			appendEntryGuardDownSince(sb);
		}
		if(unlistedSince != null) {
			appendEntryGuardUnlistedSince(sb);
		}
		return sb.toString();
	}
	
	private void appendEntryGuardLine(StringBuilder sb) {
		sb.append(StateFile.KEYWORD_ENTRY_GUARD);
		sb.append(" ");
		sb.append(nickname);
		sb.append(" ");
		sb.append(identity);
		sb.append(NL);
	}
	
	
	private void appendEntryGuardAddedBy(StringBuilder sb) {
		sb.append(StateFile.KEYWORD_ENTRY_GUARD_ADDED_BY);
		sb.append(" ");
		sb.append(identity);
		sb.append(" ");
		sb.append(version);
		sb.append(" ");
		sb.append(formatDate(createdTime));
		sb.append(NL);
	}
	
	private void appendEntryGuardDownSince(StringBuilder sb) {
		if(downSince == null) {
			return;
		}
		sb.append(StateFile.KEYWORD_ENTRY_GUARD_DOWN_SINCE);
		sb.append(" ");
		sb.append(formatDate(downSince));
		if(lastConnect != null) {
			sb.append(" ");
			sb.append(formatDate(lastConnect));
		}
		sb.append(NL);
	}
	
	private void appendEntryGuardUnlistedSince(StringBuilder sb) {
		if(unlistedSince == null) {
			return;
		}
		sb.append(StateFile.KEYWORD_ENTRY_GUARD_UNLISTED_SINCE);
		sb.append(" ");
		sb.append(formatDate(unlistedSince));
		sb.append(NL);
	}

	private String formatDate(Date date) {
		return StateFile.formatDate(date);
	}
}
