package org.torproject.jtor.config.impl;

import java.io.File;

import org.torproject.jtor.TorConfig;

public class TorConfigImpl implements TorConfig{

	public String getDataDirectory() {
		return createDataDirectory().getAbsolutePath();
	}

	private File createDataDirectory() {
		final File home = new File(System.getProperty("user.home"));
		final File dataDir =  new File(home, ".jor");
		if(!dataDir.exists())
			dataDir.mkdirs();
		return dataDir;
	}
}
