package org.torproject.jtor.control.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.torproject.jtor.control.ControlConnectionHandler;
import org.torproject.jtor.control.ControlServer;
import org.torproject.jtor.control.auth.ControlAuthenticator;

/**
 *
 * @author Merlijn Hofstra
 */
public class ControlConnectionHandlerTCP extends ControlConnectionHandler {

    private Socket s;
    private boolean running = false;

    public ControlConnectionHandlerTCP(ControlServer cs,Socket s) {
        this.s = s;
        this.cs = cs;
        running = true;
        this.start();
    }

    @Override
    public void run() {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            boolean requestedProtocolinfo = false;
            
            while (running) {
                String recv = in.readLine();
                
                System.out.println("recieved: " + recv); // TODO remove

                if (recv.toLowerCase().startsWith("quit")) {
                    disconnect();
                } else if (recv.toLowerCase().startsWith("authenticate")) {
                    if (ControlAuthenticator.authenticate(cs.getTorConfig(), recv)) {
                        authenticated = true;
                        write("250 OK");
                    } else {
                        write("515 Bad authentication");
                        disconnect();
                    }
                } else if (recv.toLowerCase().startsWith("protocolinfo")) {
                    if (!requestedProtocolinfo || authenticated) {
                        requestedProtocolinfo = !authenticated;
                        // send protocol info TODO
                    } else {
                        //error out
                        disconnect();
                    }
                } else if (authenticated) { // execute command
                	ControlCommandParser ccp = new ControlCommandParser(this);
                	ccp.execute(recv);	
                } else { // user is trying something illegal
                	disconnect();
                }
                
            }
        } catch (IOException ex) {
            Logger.getLogger(ControlConnectionHandlerTCP.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException e) {
            // may happen upon disconnect
        } finally {
            try {
                disconnect();
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(ControlConnectionHandlerTCP.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }

    public void write(String w) {
        try {
            OutputStreamWriter out = new OutputStreamWriter(s.getOutputStream());

            System.out.println("sending: " + w); // TODO remove

            out.write(w + "\r\n");
            out.flush();
        } catch (IOException ex) {
            Logger.getLogger(ControlConnectionHandlerTCP.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public synchronized void disconnect() {
        if (running) {
            running = false;
            this.interrupt();
            try {
				s.close();
			} catch (IOException e) {}
            cs.disconnectHandler(this);
        }
    }

}
