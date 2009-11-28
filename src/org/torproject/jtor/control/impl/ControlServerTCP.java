package org.torproject.jtor.control.impl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import org.torproject.jtor.TorConfig;
import org.torproject.jtor.config.impl.TorConfigImpl;
import org.torproject.jtor.control.ControlConnectionHandler;
import org.torproject.jtor.control.ControlServer;

/**
 *
 * @author Merlijn Hofstra
 */
public class ControlServerTCP extends ControlServer {

    private Vector connections = new Vector();

    public ControlServerTCP(TorConfig tc) {
        super(tc);
    }

    @Override
    public void startServer() {
        running = true;
        this.start();
    }

    @Override
    public void run() {
        ServerSocket ss = null;
        try {
            if (host != null) {
                ss = new ServerSocket(tc.getControlPort(), 0, host);
            } else {
                ss = new ServerSocket(tc.getControlPort());
            }
        } catch (IOException ex) {
            running = false;
        }
        while (running) {
            try {
                Socket s = ss.accept();
                ControlConnectionHandler cch = new ControlConnectionHandlerTCP(this, s);
                connections.add(cch);
            } catch (Throwable t) {}
        }

    }

    @Override
    public void stopServer() {
        running = false;
        this.interrupt();
        Iterator i = connections.iterator();
        while (i.hasNext()) {
            ((ControlConnectionHandler)i.next()).disconnect();
        }
    }

    public void disconnectHandler(ControlConnectionHandler cch) {
        if (connections.remove(cch)) {
            cch.disconnect();
        }
    }

    @Override
    public String getProtocol() {
        return "TCP";
    }

    public static void main (String[] arg) {
        TorConfig tc = new TorConfigImpl();
        tc.loadConf();
        ControlServer cs = new ControlServerTCP(tc);
        cs.startServer();
    }

}
