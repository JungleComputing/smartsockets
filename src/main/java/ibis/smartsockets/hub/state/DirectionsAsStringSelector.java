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
package ibis.smartsockets.hub.state;

import ibis.smartsockets.direct.DirectSocketAddress;

import java.util.LinkedList;


public class DirectionsAsStringSelector extends Selector {

    private LinkedList<String> good = new LinkedList<String>();
    private LinkedList<String> bad = new LinkedList<String>();
    private LinkedList<String> ugly = new LinkedList<String>();

    private final DirectSocketAddress client;

    public DirectionsAsStringSelector(DirectSocketAddress client) {
        this.client = client;
    }

    public boolean needAll() {
        return true;
    }

    public void select(HubDescription description) {

        // Collect the addresses of all proxies that claim to known the client
        // and are reachable from our location in a single hop. For all proxies
        // that we can only reach in multiple hops, we return the address of the
        // referring proxy instead (i.e., the 'reachable proxy' that informed us
        // of the existance of the 'unreachable proxy').
        //
        // We return the results in list, sorted by how 'good an option' they
        // are. The order is as follows:
        //
        //  1. local proxy
        //
        //  2. proxies that can reach the client directly and,
        //      a. we can connect to directly
        //      b. can connect directly to us
        //
        //  3. indirections for proxies that can reach the client directly, but
        //     which we cannot reach and,
        //      a. we can connect to directly
        //      b. can connect directly to us

        if (description.containsClient(client)) {

            if (description.isLocal()) {
                good.addFirst(description.hubAddressAsString);
            } else if (description.isReachable()) {
                good.addLast(description.hubAddressAsString);
            } else if (description.canReachMe()) {
                bad.addLast(description.hubAddressAsString);
            } else {
                HubDescription indirect = description.getIndirection();

                if (indirect != null) {
                    if (indirect.isReachable()) {
                        ugly.addFirst(indirect.hubAddressAsString);
                    } else if (indirect.canReachMe()) {
                        ugly.addLast(indirect.hubAddressAsString);
                    }
                }
            }
        }
    }

    public LinkedList<String> getResult() {

        LinkedList<String> result = new LinkedList<String>();

        result.addAll(good);
        result.addAll(bad);
        result.addAll(ugly);

        return result;
    }
}
