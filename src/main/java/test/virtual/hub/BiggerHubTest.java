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
package test.virtual.hub;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Properties;

import ibis.smartsockets.hub.Hub;
import ibis.smartsockets.util.TypedProperties;
import ibis.smartsockets.virtual.InitializationException;
import ibis.smartsockets.virtual.VirtualSocketFactory;

public class BiggerHubTest {

    private static String port = null;
    private static int clients = 10;
    private static boolean hub = true;
    private static LinkedList<String> otherHubs = new LinkedList<String>();

    private void start() throws IOException, InitializationException {

        String address = null;

        if (hub) {

            TypedProperties p = new TypedProperties();

            if (port != null) {
                p.setProperty("smartsockets.hub.port", "" + port);
            }

            Hub h = new Hub(p);
            address = h.getHubAddress().toString();

            System.out.println("Hub running at: " + address);

            h.addHubs(otherHubs.toArray(new String[0]));
        }

        for (int i=0;i<clients;i++) {

            Properties p = new Properties();
            p.setProperty("name", "S" + i);

            if (hub) {
                p.setProperty("smartsockets.hub.addresses", address);
            }

            @SuppressWarnings("unused")
            SimpleHubTest t = new SimpleHubTest(
                    VirtualSocketFactory.createSocketFactory(p, true));

            //new Thread(t).start();
        }
    }

    public static void main(String [] args) throws IOException, InitializationException {

        for (int i=0;i<args.length;i++) {
            if (args[i].equals("-no-hub")) {
                hub = false;
            } else if (args[i].equals("-clients") && i < args.length) {
                clients = Integer.parseInt(args[++i]);
            } else if (args[i].equals("-hub") && i < args.length) {
                otherHubs.add(args[++i]);
            } else if (args[i].equals("-port") && i < args.length) {
                port = args[++i];
            } else {
                System.err.println("Unknown option: " + args[i]);
            }
        }

        new BiggerHubTest().start();
    }
}


