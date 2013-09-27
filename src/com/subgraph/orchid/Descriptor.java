package com.subgraph.orchid;

import com.subgraph.orchid.data.HexDigest;

public interface Descriptor extends Document {
	enum CacheLocation { NOT_CACHED, CACHED_CACHEFILE, CACHED_JOURNAL }

	HexDigest getDescriptorDigest();
	void setLastListed(long timestamp);
	long getLastListed();
	void setCacheLocation(CacheLocation location);
	CacheLocation getCacheLocation();
	int getBodyLength();
}
