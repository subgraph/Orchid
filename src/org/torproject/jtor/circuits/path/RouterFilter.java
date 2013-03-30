package org.torproject.jtor.circuits.path;

import org.torproject.jtor.directory.Router;

public interface RouterFilter {
	boolean filter(Router router);
}
