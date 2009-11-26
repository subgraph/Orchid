/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.torproject.jtor.config.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author merlijn
 */
public class TorConfigParserImpl {

    private TorConfigParserImpl() {}

    public static Map parseFile(File in) {
        HashMap hm = new HashMap();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(in));
            String line = null;
            while ((line=reader.readLine()) != null) {
                if (line.startsWith("#") || line.matches("^\\s*$")) { // skip comments and empty lines
                    continue;
                }
                
                // strip comments after a setting
                int index = line.indexOf("#");
                if (index != -1 && !line.substring(index-1, index).equals("\\")) {
                    line = line.substring(0, index-1);
                }

                //strip any trailing whitespace
                line = line.replaceAll("\\s*$", "");
                
                int separator = line.indexOf(" ");
                String key = line.substring(0, separator);
                String value = line.substring(separator+1);

                if (hm.get(key) != null) { // handle multiple options with the same key
                	String newvalue = (String)hm.get(key) + "\n" + value;
                	hm.put(key, newvalue);
                } else {
                	hm.put(key, value);
                }
                
            }

        } catch (FileNotFoundException ex) {
            //no config file available
            return null;
        } catch (IOException e) {
            return null;
        }

        return hm;
    }

}
