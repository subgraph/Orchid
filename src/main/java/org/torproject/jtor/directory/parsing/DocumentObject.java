package org.torproject.jtor.directory.parsing;

public class DocumentObject {

	private final String keyword;
	private final StringBuilder stringContent;

	public DocumentObject(String keyword) {
		this.keyword = keyword;
		this.stringContent = new StringBuilder();
	}
	public String getKeyword() {
		return keyword;
	}

	public void addContent(String content) {
		stringContent.append(content);
		stringContent.append("\n");
	}

	public String getContent() {
		return stringContent.toString();
	}

}
