package org.torproject.jtor.directory.parsing;


public interface DocumentParser<T> {
	boolean parse(DocumentParsingResultHandler<T> resultHandler);
}
