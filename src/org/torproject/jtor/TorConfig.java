package org.torproject.jtor;

public interface TorConfig {
	String getDataDirectory();
        void setConf(String key, String value);
        String getConf(String key);
        String[] getConfArray(String key);
        void loadConf();
        void saveConf();
        void resetConf();
}
