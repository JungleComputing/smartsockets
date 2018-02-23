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


public class DirectionsSelector extends Selector {

    private LinkedList<DirectSocketAddress> good = new LinkedList<DirectSocketAddress>();
    private LinkedList<DirectSocketAddress> bad = new LinkedList<DirectSocketAddress>();

    private final DirectSocketAddress client;
    private final boolean includeLocal;

    public DirectionsSelector(DirectSocketAddress client, boolean includeLocal) {
        this.client = client;
        this.includeLocal = includeLocal;
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
        //  1. local proxy (if allowed)
        //
        //  2. proxies that can reach the client directly and that we are
        //     directly connected to.
        //
        //  3. indirections for proxies that can reach the client directly, but
        //     which we cannot reach and (provided that we are directly
        //     connected to these indirections).

        if (description.containsClient(client)) {

            if (description.isLocal() && includeLocal) {
                good.addFirst(description.hubAddress);
            } else if (description.haveConnection()) {
                good.addLast(description.hubAddress);
            } else {
                HubDescription indirect = description.getIndirection();

                if (indirect != null) {
                    if (indirect.haveConnection()) {
                       bad.addFirst(indirect.hubAddress);
                    }
                }
            }
        }
    }

    public LinkedList<DirectSocketAddress> getResult() {

        LinkedList<DirectSocketAddress> result = new LinkedList<DirectSocketAddress>();

        result.addAll(good);
        result.addAll(bad);

        return result;
    }
}
