/*
 * Copyright 2010 Vrije Universiteit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ibis.smartsockets.util.net;


import ibis.smartsockets.util.InetAddressCache;
import ibis.smartsockets.util.NetworkUtils;
import ibis.smartsockets.util.net.NetworkInfo;

import java.util.List;
import java.util.StringTokenizer;


public class LinuxNetworkInfoParser extends NetworkInfoParser {

    private static final String [][] commands = new String [][] {
        new String [] { "ifconfig" },
        new String [] { "/sbin/ifconfig" },
        new String [] { "/bin/ifconfig" }
    };

    public LinuxNetworkInfoParser() {
        super("Linux");
    }

    public boolean parse(byte [] output, List<NetworkInfo> info) {

        boolean result = false;
        StringBuffer tmp = new StringBuffer(new String(output));

        int start = 0;

        for (int i=1;i<tmp.length();i++) {
            if (tmp.charAt(i) == '\n' && tmp.charAt(i-1) == '\n') {
                result = parseBlock(tmp.substring(start, i), info) || result;
                start = i;
            }
        }

        if (start < tmp.length()-1) {
            result = parseBlock(tmp.substring(start, tmp.length()), info) || result;
        }

        return result;
    }
    
    private boolean parseBlock(String tmp, List<NetworkInfo> info) {

        // // System.out.println("Parse block:\n" + tmp + "\n\n");

        StringTokenizer tokenizer = new StringTokenizer(tmp, "\n");

        NetworkInfo nw = new NetworkInfo();

        while (tokenizer.hasMoreTokens()) {

            String line = tokenizer.nextToken().trim();

            // The hardware adress (MAC) is behind a device specific name, such as "ether" or "infiniband"
            String mac = getField(line, "ether");

            if (mac == null) { 
                mac = getField(line, "infiniband");
            }

            if (mac != null && isMacAddress(mac)) {
                nw.mac = NetworkUtils.MACStringToBytes(mac);
            }
            
            String t = getIPv4Field(line, "inet ");

            if (t != null) {
                // .println("Got ipv4 " + t);
                try {
                    nw.ipv4 = InetAddressCache.getByName(t);
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }

            t = getIPv4Field(line, "broadcast ");

            if (t != null) {
                // System.out.println("Got bcast " + t);
                nw.broadcast = ipStringToBytes(t);
            }

            t = getIPv4Field(line, "netmask ");

            if (t != null) {
                // System.out.println("Got mask " + t);
                nw.netmask = ipStringToBytes(t);
            }

            t = getIPv6Field(line, "inet6 ");

            if (t != null) {
                // System.out.println("Got ipv6 " + t);
                try {
                    nw.ipv6 = InetAddressCache.getByName(t);
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
        }

        info.add(nw);

        return true;
    }

    String[] getCommand(int number) {
        return commands[number];
    }

    int numberOfCommands() {
        return commands.length;
    }
}
