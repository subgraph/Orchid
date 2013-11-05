package com.subgraph.orchid;

public interface DirectoryDownloader {
	void start(Directory directory);
	RouterDescriptor downloadBridgeDescriptor(Router bridge);
}
