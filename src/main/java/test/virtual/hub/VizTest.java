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

import ibis.smartsockets.direct.DirectSocketAddress;
import ibis.smartsockets.virtual.VirtualSocketFactory;

import java.io.IOException;
import java.util.Arrays;

public class VizTest {

    private VirtualSocketFactory factory;
    private int state = 0;
    private boolean done = false;

    public VizTest(VirtualSocketFactory factory) throws IOException {

        this.factory = factory;

    //    factory.getServiceLink().registerProperty("smartsockets.viz",
    //            "S^state=" + state++);

        System.out.println("##### VIZ TEST START #####");


        boolean ok = factory.getServiceLink().registerProperty("smartsockets.viz", "I^" + "ibis0"
                  + "," + "@little@house@ontheprairy");

        if (!ok) {
            System.out.println("EEP: registration failed!");
        }

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

    //            factory.getServiceLink().updateProperty("smartsockets.viz",
    //                    "S^state=" + state++);

                String tmp = "I^" + "ibis" + state++  + "," + "@little@house@ontheprairy";

                System.out.println("PROP: " + tmp);

                boolean ok = factory.getServiceLink().updateProperty("smartsockets.viz", tmp);

                if (!ok) {
                    System.out.println("EEP: update failed!");
                }

                if (state >= 5) {
                    done();
                }
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

        try {
            Thread.sleep(50000);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    public static void main(String[] args) {

        try {
            VirtualSocketFactory factory =
                VirtualSocketFactory.createSocketFactory((java.util.Properties) null, true);

            System.out.println("Created socket factory");

            new VizTest(factory).run();

        } catch (Exception e) {
            System.err.println("Oops: " + e);
            e.printStackTrace(System.err);
        }
    }
}
