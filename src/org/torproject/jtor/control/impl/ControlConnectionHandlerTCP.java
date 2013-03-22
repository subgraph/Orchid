package org.torproject.jtor.control.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.logging.Logger;

import org.torproject.jtor.control.ControlConnectionHandler;
import org.torproject.jtor.control.ControlServer;

/**
 *
 * @author Merlijn Hofstra
 */
public class ControlConnectionHandlerTCP extends ControlConnectionHandler {
	private final static Logger logger = Logger.getLogger(ControlConnectionHandlerTCP.class.getName());
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
            
            while (running) {
                String recv = in.readLine();
                
                recv.length(); // trigger NullPointerException
                logger.fine("Control Connection TCP: received " + recv);
                
                eq.writeQueue(this);
                
                ControlCommandParser.execute(this, recv);
            }
            
        } catch (IOException ex) {
        	logger.fine("Control Connection TCP: IOException during receiving");
        } catch (NullPointerException e) {
            // may happen upon disconnect
        } finally {
            try {
                disconnect();
                in.close();
            } catch (IOException ex) {}
        }
        
    }

	public void write(String w) {
        try {
            OutputStreamWriter out = new OutputStreamWriter(s.getOutputStream());

            logger.fine("Control Connection TCP:  sending " + w);

            out.write(w + "\r\n");
            out.flush();
        } catch (IOException ex) {
        	logger.fine("Control Connection TCP: IOException during sending");
        	disconnect();
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
