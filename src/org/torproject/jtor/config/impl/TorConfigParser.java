package org.torproject.jtor.config.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.torproject.jtor.TorConfig.ConfigVarType;

public class TorConfigParser {
	
	public Object parseValue(String value, ConfigVarType type) {
		switch(type) {
		case BOOLEAN:
			return Boolean.parseBoolean(value);
		case INTEGER:
			return Integer.parseInt(value);
		case INTERVAL:
			return parseIntervalValue(value);
		case PATH:
			return parseFileValue(value);
		case PORTLIST:
			return parseIntegerList(value);
		case STRING:
			return value;
		case STRINGLIST:
			return parseCSV(value);
		}
		throw new IllegalArgumentException();
	}

	private File parseFileValue(String value) {
		if(value.startsWith("~/")) {
			final File home = new File(System.getProperty("user.home"));
			return new File(home, value.substring(2));
		}
		return new File(value);
	}
	private TorConfigInterval parseIntervalValue(String value) {
		return TorConfigInterval.createFrom(value);
	}
	
	private List<Integer> parseIntegerList(String value) {
		final List<Integer> list = new ArrayList<Integer>();
		for(String s: value.split(",")) {
			list.add(Integer.parseInt(s));
		}
		return list;
	}
	
	private List<String> parseCSV(String value) {
		final List<String> list = new ArrayList<String>();
		for(String s: value.split(",")) {
			list.add(s);
		}
		return list;
	}
}
