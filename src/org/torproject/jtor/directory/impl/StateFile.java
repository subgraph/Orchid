package org.torproject.jtor.directory.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.torproject.jtor.Tor;
import org.torproject.jtor.directory.Directory;
import org.torproject.jtor.directory.GuardEntry;
import org.torproject.jtor.directory.Router;

public class StateFile {
	private final static Logger logger = Logger.getLogger(StateFile.class.getName());
	
	private final static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private final static int DATE_LENGTH = 19;
	
	final static String KEYWORD_ENTRY_GUARD = "EntryGuard";
	final static String KEYWORD_ENTRY_GUARD_ADDED_BY = "EntryGuardAddedBy";
	final static String KEYWORD_ENTRY_GUARD_DOWN_SINCE = "EntryGuardDownSince";
	final static String KEYWORD_ENTRY_GUARD_UNLISTED_SINCE = "EntryGuardUnlistedSince";
	
	private final List<GuardEntryImpl> guardEntries = new ArrayList<GuardEntryImpl>();
	
	private class Line {
		final String line;
		int offset;
		
		Line(String line) {
			this.line = line;
			offset = 0;
		}
		
		private boolean hasChars() {
			return offset < line.length();
		}
		
		private char getChar() {
			return line.charAt(offset);
		}

		private void incrementOffset(int n) {
			offset += n;
			if(offset > line.length()) {
				offset = line.length();
			}
		}
	
		private void skipWhitespace() {
			while(hasChars() && Character.isWhitespace(getChar())) {
				offset += 1;
			}
		}
		
		String nextToken() {
			skipWhitespace();
			if(!hasChars()) {
				return null;
			}
		
			final StringBuilder token = new StringBuilder();
			while(hasChars() && !Character.isWhitespace(getChar())) {
				token.append(getChar());
				offset += 1;
			}
			return token.toString();
		}
		
		Date parseDate() {
			skipWhitespace();
			if(!hasChars()) {
				return null;
			}
			try {
				final Date date = dateFormat.parse(line.substring(offset));
				incrementOffset(DATE_LENGTH);
				return date;
			} catch (ParseException e) {
				return null;
			}
		}
	}

	static String formatDate(Date date) {
		return dateFormat.format(date);
	}

	private final DirectoryStoreImpl directoryStore;
	private final Directory directory;
	
	StateFile(DirectoryStoreImpl store, Directory directory) {
		this.directoryStore = store;
		this.directory = directory;
	}

	public GuardEntry createGuardEntryFor(Router router) {
		final GuardEntryImpl entry = new GuardEntryImpl(directory, this, router.getNickname(), router.getIdentityHash().toString());
		final String version = Tor.getImplementation() + "-" + Tor.getVersion();
		entry.setVersion(version);
		entry.setCreatedTime(new Date());
		return entry;
	}

	public List<GuardEntry> getGuardEntries() {
		synchronized (guardEntries) {
			return new ArrayList<GuardEntry>(guardEntries);
		}
	}

	public void removeGuardEntry(GuardEntry entry) {
		synchronized (guardEntries) {
			guardEntries.remove(entry);
			writeFile();
		}
	}

	public void addGuardEntry(GuardEntry entry) {
		addGuardEntry(entry, true);
	}

	private void addGuardEntry(GuardEntry entry, boolean writeFile) {
		synchronized(guardEntries) {
			if(guardEntries.contains(entry)) {
				return;
			}
			final GuardEntryImpl impl = (GuardEntryImpl) entry;
			guardEntries.add(impl);
			synchronized (impl) {
				impl.setAddedFlag();
				if(writeFile) {
					writeFile();
				}
			}
		}
	}

	void writeFile() {
		directoryStore.saveStateFile(this);
	}
	
	void writeFile(Writer writer) throws IOException {
		synchronized(guardEntries) {
			for(GuardEntryImpl entry : guardEntries) {
				writer.write(entry.writeToString());
			}
		}
	}

	void parseFile(Reader reader) throws IOException {
		final BufferedReader br = new BufferedReader(reader);
		synchronized (guardEntries) {
			guardEntries.clear();
			loadGuardEntries(br);
		}
	}

	private void loadGuardEntries(BufferedReader reader) throws IOException {
		GuardEntryImpl currentEntry = null;
		while(true) {
			Line line = readLine(reader);
			if(line == null) {
				addEntryIfValid(currentEntry);
				return;
			}
			currentEntry = processLine(line, currentEntry);
		}
	}

	private GuardEntryImpl processLine(Line line, GuardEntryImpl current) {
		final String keyword = line.nextToken();
		if(keyword == null) {
			return current;
		} else if(keyword.equals(KEYWORD_ENTRY_GUARD)) {
			addEntryIfValid(current);
			return processEntryGuardLine(line);
		} else if(keyword.equals(KEYWORD_ENTRY_GUARD_ADDED_BY)) {
			processEntryGuardAddedBy(line, current);
			return current;
		} else if(keyword.equals(KEYWORD_ENTRY_GUARD_DOWN_SINCE)) {
			processEntryGuardDownSince(line, current);
			return current;
		} else if(keyword.equals(KEYWORD_ENTRY_GUARD_UNLISTED_SINCE)) {
			processEntryGuardUnlistedSince(line, current);
			return current;
		} else {
			return current;
		}
	}
	
	private GuardEntryImpl processEntryGuardLine(Line line) {
		final String name = line.nextToken();
		final String identity = line.nextToken();
		if(name == null || name.isEmpty() || identity == null || identity.isEmpty()) {
			
		}
		return new GuardEntryImpl(directory, this, name, identity);
	}
	
	private void processEntryGuardAddedBy(Line line, GuardEntryImpl current) {
		if(current == null) {
			logger.warning("EntryGuardAddedBy line seen before EntryGuard in state file");
			return;
		}
		final String identity = line.nextToken();
		final String version = line.nextToken();
		final Date created = line.parseDate();
		if(identity == null || identity.isEmpty() || version == null || version.isEmpty() || created == null) {
			logger.warning("Missing EntryGuardAddedBy field in state file");
			return;
		}
		current.setVersion(version);
		current.setCreatedTime(created);
	}
	
	private void processEntryGuardDownSince(Line line, GuardEntryImpl current) {
		if(current == null) {
			logger.warning("EntryGuardDownSince line seen before EntryGuard in state file");
			return;
		}
		
		final Date downSince = line.parseDate();
		final Date lastTried = line.parseDate();
		if(downSince == null) {
			logger.warning("Failed to parse date field in EntryGuardDownSince line in state file");
			return;
		}
		current.setDownSince(downSince, lastTried);
	}
	
	private void processEntryGuardUnlistedSince(Line line, GuardEntryImpl current) {
		if(current == null) {
			logger.warning("EntryGuardUnlistedSince line seen before EntryGuard in state file");
			return;
		}
		final Date unlistedSince = line.parseDate();
		if(unlistedSince == null) {
			logger.warning("Failed to parse date field in EntryGuardUnlistedSince line in state file");
			return;
		}
		current.setUnlistedSince(unlistedSince);
	}

	private void addEntryIfValid(GuardEntryImpl entry) {
		if(isValidEntry(entry)) {
			addGuardEntry(entry, false);
		}
	}

	private boolean isValidEntry(GuardEntryImpl entry) {
		return entry != null &&
				entry.getNickname() != null && 
				entry.getIdentity() != null && 
				entry.getVersion() != null && 
				entry.getCreatedTime() != null;
	}

	private Line readLine(BufferedReader reader) throws IOException {
		final String str = reader.readLine();
		if(str == null) {
			return null;
		}
		return new Line(str);
	}
}
