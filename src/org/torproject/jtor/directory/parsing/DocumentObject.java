package org.torproject.jtor.directory.parsing;

public class DocumentObject {
	
	final private String keyword;
	final private StringBuilder stringContent;
	
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
