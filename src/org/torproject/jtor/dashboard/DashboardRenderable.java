package org.torproject.jtor.dashboard;

import java.io.IOException;
import java.io.PrintWriter;

public interface DashboardRenderable {
	void dashboardRender(PrintWriter writer, int flags) throws IOException;
}
