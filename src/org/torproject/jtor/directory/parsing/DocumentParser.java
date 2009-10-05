package org.torproject.jtor.directory.parsing;

import java.util.List;

import org.torproject.jtor.directory.Directory;

public interface DocumentParser<T> {
	void parse();
	void parseAndAddToDirectory(Directory directory);
	List<T> getDocuments();
}
