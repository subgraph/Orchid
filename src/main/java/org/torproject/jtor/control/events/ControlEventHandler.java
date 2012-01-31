package org.torproject.jtor.control.events;

import org.torproject.jtor.events.EventHandler;

public abstract class ControlEventHandler implements EventHandler {
	
	protected ControlEventQueue ceq;
	
	public ControlEventHandler(ControlEventQueue ceq) {
		this.ceq = ceq;
	}
	
	public ControlEventHandler() {}

}
