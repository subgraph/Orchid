package org.torproject.jtor.control.commands;

import org.torproject.jtor.control.ControlConnectionHandler;

public class ControlCommandUseFeature {

	public static void handleUseFeature(ControlConnectionHandler cch, String feature) {
		// currently all listed features are enabled by default
		cch.write("250 OK");
	}
}
