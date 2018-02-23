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
package ibis.smartsockets.util;

import ibis.smartsockets.util.ThreadPool;

import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.javawi.jstun.test.DiscoveryInfo;
import de.javawi.jstun.test.DiscoveryTest;

public class STUN {

    private static Logger logger =
        LoggerFactory.getLogger(STUN.class.getName());

    private static String [] DEFAULT_SERVERS =
        new String [] { "stun.voipbuster.com",
                        "stun.fwd.org",
                        "iphone-stun.freenet.de",
                        "stun.xten.net",
                        "stun.fwdnet.net" };

    private static InetAddress external;

    static class Discovery implements Runnable {

        private final InetAddress iaddress;
        private final int port;
        private final String server;

        private boolean done = false;
        private DiscoveryInfo result;


        public Discovery(InetAddress iaddress, int port, String server) {
            this.iaddress = iaddress;
            this.port = port;
            this.server = server;
        }

        public synchronized DiscoveryInfo getResult() {

            while (!done) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    // ignore
                }
            }

            return result;
        }

        public synchronized boolean done() {
            return done;
        }

        public void run() {
            try {
                if (logger.isInfoEnabled()) {
                    logger.info("STUN discovery initiated on: " + iaddress
                            + ":" + port);
                }

                DiscoveryTest test = new DiscoveryTest(iaddress, port, server, 3478);

                synchronized (this) {
                    result = test.test();

                    if (logger.isInfoEnabled()) {
                        logger.info("STUN discovery done on: " + iaddress + ":" + port
                                + "\n" + result);
                    }

                    done = true;
                    notifyAll();
                }


            } catch (Exception e) {
                logger.warn("STUN discovery on " + iaddress + " failed!", e);
            }
        }
    }

    private static void getExternalAddress(String server, int timeout) {

        if (logger.isInfoEnabled()) {
            logger.info("Trying to determine external address using "
                    + "STUN server:" + server);
        }

        InetAddress [] addresses = NetworkUtils.getAllHostAddresses();
        Discovery [] tmp = new Discovery[addresses.length];

        if (logger.isInfoEnabled()) {
            logger.info("Network addresses available: " + addresses.length);
        }

        int wait = 25;
        int count = 0;

        int port = 3478;

        // For each network address (except loopback) we start a thread that
        // tries to find the external address using STUN.
        for (int i=0;i<addresses.length;i++) {
            if (!addresses[i].isLoopbackAddress()) {
                count++;
                tmp[i] = new Discovery(addresses[i], port + i, server);
                ThreadPool.createNew(tmp[i], "STUN " + addresses[i]);
            }
        }

        long end = System.currentTimeMillis() + timeout;

        while (true) {

            // Next, we gather the results and try to find an external address.
            for (int i=0;i<tmp.length;i++) {
                if (tmp[i] != null && tmp[i].done()) {
                    DiscoveryInfo info = tmp[i].getResult();
                    tmp[i] = null;
                    count--;

                    if (info == null) {
                        if (logger.isInfoEnabled()) {
                            logger.info("STUN failed for " + addresses[i]);
                        }
                    } else {
                        if (logger.isInfoEnabled()) {
                            logger.info("STUN result for " + addresses[i] + ":\n" + info);
                        }

                        if (info.isOpenAccess()) {
                            external = addresses[i];
                        } else {
                            external = info.getPublicIP();
                        }

                        if (logger.isInfoEnabled()) {
                            logger.info("Found external address: " + external
                                    + " using server " + server);
                        }
                        return;
                    }
                }
            }

            if (count == 0) {
                // No more results to wait for....
                return;
            }

            if (timeout > 0 && System.currentTimeMillis() > end) {
                // Time has exceeded (threads should die automatically)....
                return;
            }

            // Sleep for half a second to prevent busy waiting.
            try {
                Thread.sleep(wait);
            } catch (Exception e) {
                // ignore
            }

            if (wait < 1000) {
                wait *= 2;
            }
        }
    }

    public static InetAddress getExternalAddress(String [] servers, int timeout) {

        if (external != null) {
            return external;
        }

        if (servers == null || servers.length == 0) {
            servers = DEFAULT_SERVERS;
        }

        for (int i=0;i<servers.length;i++) {
            getExternalAddress(servers[i], 0);

            if (external != null) {
                break;
            }
        }

        return external;
    }

    public static InetAddress getExternalAddress(int timeout) {
        return getExternalAddress((String []) null, timeout);
    }

    public static InetAddress getExternalAddress(String [] servers) {
        return getExternalAddress(servers, 0);
    }

    public static InetAddress getExternalAddress() {
        return getExternalAddress((String []) null, 0);
    }

    public static void main(String [] args) {

        System.out.println("Attempting to retrieve external address....");

        long start = System.currentTimeMillis();

        InetAddress ad = getExternalAddress();

        long end = System.currentTimeMillis();

        System.out.println("Got address " + NetworkUtils.ipToString(ad)
                + " after " + (end-start) + " ms.");
    }
}
