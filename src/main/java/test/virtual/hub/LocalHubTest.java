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

import java.util.Arrays;
import java.util.HashMap;

import ibis.smartsockets.SmartSocketsProperties;
import ibis.smartsockets.direct.DirectSocketAddress;
import ibis.smartsockets.virtual.VirtualSocketFactory;

public class LocalHubTest {

    public static void main(String[] args) {

        try {
            HashMap<String, String> p = new HashMap<String, String>();
            p.put(SmartSocketsProperties.START_HUB, "true");
            p.put(SmartSocketsProperties.HUB_DELEGATE, "true");

            VirtualSocketFactory factory = VirtualSocketFactory
                    .createSocketFactory(p, true);

            System.out.println("Created socket factory");

            int count = 0;

            do {
                DirectSocketAddress[] hubs = factory.getKnownHubs();

                System.out.println("Known hubs: " + Arrays.deepToString(hubs));

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // ignore
                }
            } while (count++ < 25);

            factory.end();

            System.out.println("Done!");

        } catch (Exception e) {
            System.err.println("Oops: " + e);
            e.printStackTrace(System.err);
        }
    }
}
