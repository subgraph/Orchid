package org.torproject.jtor;

import java.util.Arrays;

import org.torproject.jtor.circuits.Circuit;
import org.torproject.jtor.circuits.CircuitBuildHandler;
import org.torproject.jtor.circuits.CircuitNode;
import org.torproject.jtor.circuits.Connection;
import org.torproject.jtor.directory.Directory;

public class TorTest {

	private final Tor tor;
	private final Directory directory;
	TorTest() {
		tor = new Tor();
		directory = tor.getDirectory();
	}
	
	
	
	void circuitTest()  {
		
		String[] path = {  "AnonymousRelay", "CodeGnomeTor1", "bambi", "TorVidalia"};
		
		Circuit c = tor.createCircuitFromNicknames(Arrays.asList(path));
		
		c.openCircuit(new CircuitBuildHandler() {
			
			public void nodeAdded(CircuitNode node) {
				System.out.println("Node added: "+ node.getRouter().getNickname());				
			}
			
			public void connectionFailed(String reason) {
				System.out.println("Connect to entry router failed: "+ reason);				
			}
			
			public void connectionCompleted(Connection connection) {
				System.out.println("Connected to entry router: "+ connection.getRouter().getNickname());				
			}
			
			public void circuitBuildFailed(String reason) {
				System.out.println("Circuit creation failed: "+ reason);				
			}

			public void circuitBuildCompleted(Circuit circuit) {
				System.out.println("Circuit creation completed successfully");
				
			}
		});
		//c.extendCircuit(tor.getDirectory().getRouterByName("bambi"));
		
		System.out.println("DONE");
	}
	
	
	public static void main(String[] args) {
		
		TorTest test = new TorTest();
		test.tor.start();
		
	}
}
