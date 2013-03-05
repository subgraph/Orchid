package org.torproject.jtor.control.events;

import org.torproject.jtor.events.Event;

public class NewConsensusHandler extends ControlEventHandler {

	public NewConsensusHandler(ControlEventQueue ceq) {
		super(ceq);
	}

	public void handleEvent(Event event) {
		getControlEventQueue().addMessage("650+NEWCONSENSUS");
		//TODO add networkstatus here
		getControlEventQueue().addMessage("650 OK");
	}

}
