package org.torproject.jtor.circuits.path;

import org.torproject.jtor.Router;

public interface RouterFilter {
	boolean filter(Router router);
}
