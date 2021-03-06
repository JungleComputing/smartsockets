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
package test.virtual.simple;


import ibis.smartsockets.hub.servicelink.ClientInfo;
import ibis.smartsockets.virtual.InitializationException;
import ibis.smartsockets.virtual.VirtualServerSocket;
import ibis.smartsockets.virtual.VirtualSocket;
import ibis.smartsockets.virtual.VirtualSocketAddress;
import ibis.smartsockets.virtual.VirtualSocketFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class ConnectTest {

    private static final int SERVERPORT = 42611;

    private static final int REPEAT = 10;
    private static final int COUNT = 1000;
    private static final int TIMEOUT = 1000;

    private static final boolean PINGPONG = false;

    private static VirtualSocketFactory sf;

    private static HashMap<String, Object> connectProperties;

    private static boolean pingpong = PINGPONG;
    private static int count = COUNT;
    private static int timeout = TIMEOUT;

    private static boolean fillTimeout = false;

    private static boolean sleep = false;

    private static Random rand = new Random();

    public static void connect(VirtualSocketAddress target) {

        long [] detailedDirect = new long[1+2*target.machine().numberOfAddresses()];
        long [] detailedVirtual = new long[5];

        connectProperties.put("direct.detailed.timing", detailedDirect);
        connectProperties.put("virtual.detailed.timing", detailedVirtual);

        int failed = 0;

        long time = System.currentTimeMillis();

        for (int c=0;c<count;c++) {

            InputStream in = null;
            OutputStream out = null;
            VirtualSocket s = null;

            try {

                s = sf.createClientSocket(target, timeout, fillTimeout,
                        connectProperties);

                if (pingpong) {
                    s.setTcpNoDelay(true);

                    out = s.getOutputStream();

                    out.write(42);
                    out.flush();

                    in = s.getInputStream();
                    int tmp = in.read();

                    if (tmp != 42) {
                        System.err.println("EEP: got " + tmp + " instead of 42");
                    }

                }

                /*
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                    // TODO: handle exception
                }*/

            } catch (Exception e) {
                time = System.currentTimeMillis() - time;

                System.out.println("Failed to create connection to " + target);
                e.printStackTrace();

                failed++;
            } finally {
                VirtualSocketFactory.close(s, out, in);
            }
        }

        time = System.currentTimeMillis() - time;

        System.out.println(count + " connections in " + time
                + " ms. -> " + (((double) time) / count)
                + "ms/conn, Failed: " + failed);

        System.out.println("Details direct : " + Arrays.toString(detailedDirect));
        Arrays.fill(detailedDirect, 0);

        System.out.println("Details virtual: " + Arrays.toString(detailedVirtual));
        Arrays.fill(detailedVirtual, 0);

        if (sleep) {
            try {
                Thread.sleep(1000 + rand.nextInt(5000));
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    public static void accept(String id) throws IOException {

        System.out.println("Creating server socket");

        VirtualServerSocket ss = sf.createServerSocket(SERVERPORT, 0, connectProperties);

        System.out.println("Created server on " + ss.getLocalSocketAddress());

        if (id != null) {
            sf.getServiceLink().registerProperty(id,
                    ss.getLocalSocketAddress().toString());
        }

        System.out.println("Server waiting for connections");

        while (true) {

            InputStream in = null;
            OutputStream out = null;

            try {
                VirtualSocket s = ss.accept();

                if (pingpong) {
                    s.setTcpNoDelay(true);

                    in = s.getInputStream();
                    int tmp = in.read();

                    if (tmp != 42) {
                        System.err.println("EEP: got " + tmp + " instead of 42");
                    }

                    out = s.getOutputStream();
                    out.write(42);
                    out.flush();
                }

                VirtualSocketFactory.close(s, out, in);
            } catch (Exception e) {
                System.out.println("Server got exception " + e);
                e.printStackTrace();
            }
        }
    }

    public static void main(String [] args) throws IOException {

        try {
            sf = VirtualSocketFactory.createSocketFactory();
        } catch (InitializationException e1) {
            System.out.println("Failed to create socketfactory!");
            e1.printStackTrace();
            System.exit(1);
        }

        connectProperties = new HashMap<String, Object>();

        String id = null;

        int targets = args.length;
        int repeat = REPEAT;

        for (int i=0;i<args.length;i++) {
            if (args[i].equals("-repeat")) {
                repeat = Integer.parseInt(args[i+1]);
                args[i+1] = null;
                args[i] = null;
                targets -= 2;
                i++;

            } else if (args[i].equals("-count")) {
                count = Integer.parseInt(args[i+1]);
                args[i+1] = null;
                args[i] = null;
                targets -= 2;
                i++;

            } else if (args[i].equals("-timeout")) {
                timeout = Integer.parseInt(args[i+1]);
                args[i+1] = null;
                args[i] = null;
                targets -= 2;
                i++;

            } else if (args[i].equals("-fill")) {
                fillTimeout = true;
                args[i] = null;
                targets--;

            } else if (args[i].equals("-sleep")) {
                sleep = true;
                args[i] = null;
                targets--;

            } else if (args[i].equals("-ssh")) {
                connectProperties.put("allowSSH", "true");
                args[i] = null;
                targets--;

            } else if (args[i].equals("-pingpong")) {
                pingpong = true;
                args[i] = null;
                targets--;

            } else if (args[i].equals("-cache")) {
                connectProperties.put("cache.winner", null);
                args[i] = null;
                targets--;

            } else if (args[i].equals("-id")) {
                id = args[i+1];
                args[i] = args[i+1] = null;
                targets -= 2;
                i++;
            }

        }

        VirtualSocketAddress [] targetAds = new VirtualSocketAddress[targets];
        int index = 0;

        for (int i=0;i<args.length;i++) {
            if (args[i] != null) {
                targetAds[index++] = new VirtualSocketAddress(args[i]);
            }
        }

        if (targets == 0 && id != null) {
            // Check if we are using an ID instead of a target...
            ClientInfo [] info = sf.getServiceLink().clients(id);

            if (info != null && info.length > 0) {

                index = 0;
                targetAds = new VirtualSocketAddress[info.length];

                for (ClientInfo c : info) {
                    String address = c.getProperty(id);

                    if (address != null) {
                        targetAds[index++] = new VirtualSocketAddress(address);
                    }
                }
            }
        }

        if (targetAds.length > 0) {

            for (VirtualSocketAddress a : targetAds) {

                if (a == null) {
                    continue;
                }

                if (sleep) {
                    try {
                        Thread.sleep(1000 + rand.nextInt(5000));
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                }

                System.out.println("Creating connection to " + a);

                for (int r=0;r<repeat;r++) {
                    connect(a);
                }
            }
        } else {
            accept(id);
        }

        sf.printStatistics("");
    }
}
