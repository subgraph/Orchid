package org.torproject.jtor.config.impl;

import java.io.File;
import java.util.Map;
import org.torproject.jtor.TorConfig;

public class TorConfigImpl implements TorConfig{
    
    private String configFile = "torrc";
    private File dataDir = new File(new File(System.getProperty("user.home")), ".jtor");
    private Map opts;

        public TorConfigImpl() {
            createDataDirectory();
        }

	public String getDataDirectory() {
		return dataDir.getAbsolutePath();
	}

	private File createDataDirectory() {
		if(!dataDir.exists())
			dataDir.mkdirs();
		return dataDir;
	}

    public void setConf(String key, String value) {
    	try {
    		if (value.equals("")) {
    			value = null;
    		}
    	} catch (Throwable t) {}
        opts.put(key, value);
    }

    public String getConf(String key) {
        return (String) opts.get(key);
    }

    public void loadConf() {
        opts = TorConfigParserImpl.parseFile(new File(dataDir, configFile));
    }

    public void saveConf() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void resetConf() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

	public String[] getConfArray(String key) {
		String value = (String)opts.get(key);
		return value.split("\n");
	}
}
