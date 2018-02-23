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
import java.util.Arrays;

import ibis.smartsockets.direct.DirectSocketAddress;
import ibis.smartsockets.virtual.VirtualSocketFactory;

public class SimpleHubTest implements Runnable {

    private VirtualSocketFactory factory;
    private int state = 0;
    private boolean done = false;

    public SimpleHubTest(VirtualSocketFactory factory) throws IOException {

        this.factory = factory;

        factory.getServiceLink().registerProperty("smartsockets.viz",
                "S^state=" + state++);
    }

    public synchronized void done() {
        done = true;
    }

    public synchronized boolean getDone() {
        return done;
    }

    public void run() {

        try {
            while (!getDone()) {
                DirectSocketAddress[] hubs = factory.getKnownHubs();

                System.out.println("Known hubs: " + Arrays.deepToString(hubs));

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    // ignore
                }

                factory.getServiceLink().updateProperty("smartsockets.viz",
                        "S^state=" + state++);
            }

        } catch (Exception e) {
            System.err.println("Oops: " + e);
            e.printStackTrace(System.err);
        } finally {
            try {
                factory.end();
            } catch (Exception e) {
                // ignore
            }
        }

        System.out.println("Done!");
    }

    public static void main(String[] args) {

        try {
            VirtualSocketFactory factory =
                VirtualSocketFactory.createSocketFactory((java.util.Properties) null, true);

            System.out.println("Created socket factory");

            new SimpleHubTest(factory).run();

        } catch (Exception e) {
            System.err.println("Oops: " + e);
            e.printStackTrace(System.err);
        }
    }
}
