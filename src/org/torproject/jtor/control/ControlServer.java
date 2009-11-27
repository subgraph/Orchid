package org.torproject.jtor.control;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.net.InetAddress;
import org.torproject.jtor.TorConfig;
import org.torproject.jtor.control.auth.PasswordDigest;

/**
 *
 * @author Merlijn Hofstra
 */
public abstract class ControlServer extends Thread {

    protected InetAddress host;
    protected TorConfig tc;
    protected boolean running = false;

    public abstract void startServer();
    public abstract void stopServer();
    public abstract void disconnectHandler(ControlConnectionHandler cch);
    public abstract String getProtocol();

    public ControlServer(TorConfig tc) {
        this.tc = tc;
        if (tc.isCookieAuthentication()) {
        	// write out a cookie file
        	File cookie = new File(tc.getDataDirectory(), "control_auth_cookie");
        	cookie.delete();
        	PasswordDigest magic = PasswordDigest.generateDigest();
        	try {
				FileOutputStream fos = new FileOutputStream(cookie);
				fos.write(magic.getHashedPassword().substring(3).getBytes());
				fos.flush();
				fos.close();
			} catch (Throwable t) {
				// TODO Auto-generated catch block
				t.printStackTrace();
			}
        	
        }
    }
    
    public void setInetAddress(InetAddress host) {
        this.host = host;
    }

    public boolean authenticate(String input) {
        if (input == null) {
            return false;
        }
        
        String auth = input.substring(13);
        
        if (tc.isCookieAuthentication()) {
        	try {
        		File cookie = new File(tc.getDataDirectory(), "control_auth_cookie");
				BufferedReader reader = new BufferedReader(new FileReader(cookie));
				String magic = reader.readLine();
				reader.close();
				return magic.equals(auth);
			} catch (Throwable t) {
				return false;
			}
        }
        
        if (tc.getHashedControlPassword() != null) {
            // generate our control password
            PasswordDigest cp = new PasswordDigest(tc.getHashedControlPassword());
            return cp.verifyPassword(auth);
        }

        return true; // no auth required
    }

    public boolean isRunning() {
        return running;
    }
    
    public TorConfig getTorConfig() {
    	return tc;
    }

}
