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
package ibis.smartsockets.hub;

import java.util.HashMap;
import java.util.Map;

import ibis.smartsockets.direct.DirectSocketAddress;
import ibis.smartsockets.hub.connections.ClientConnection;
import ibis.smartsockets.hub.connections.HubConnection;
import ibis.smartsockets.hub.connections.MessageForwardingConnection;

public class Connections {

    private final Map<DirectSocketAddress, HubConnection> hubs;
    private final Map<DirectSocketAddress, ClientConnection> clients;

    public Connections() {
        hubs = new HashMap<DirectSocketAddress, HubConnection>();
        clients = new HashMap<DirectSocketAddress, ClientConnection>();
    }

    public synchronized void put(DirectSocketAddress a, ClientConnection c) {
        clients.put(a, c);
    }

    public synchronized void put(DirectSocketAddress a, HubConnection c) {
        hubs.put(a, c);
    }

    public synchronized HubConnection getHub(DirectSocketAddress a) {
        return hubs.get(a);
    }

    public synchronized ClientConnection getClient(DirectSocketAddress a) {
        return clients.get(a);
    }

    public synchronized boolean removeClient(DirectSocketAddress a) {
        return (clients.remove(a) != null);
    }

    public synchronized boolean removeHub(DirectSocketAddress a) {
        return (hubs.remove(a) != null);
    }

    public synchronized MessageForwardingConnection getAny(
            DirectSocketAddress a) {

        MessageForwardingConnection tmp = getHub(a);

        if (tmp != null) {
            return tmp;
        }

        return getClient(a);
    }

    public synchronized int numberOfConnections() {
        return clients.size() + hubs.size();
    }

    public synchronized int numberOfClients() {
        return clients.size();
    }

    public synchronized int numberOfHubs() {
        return hubs.size();
    }

    public synchronized DirectSocketAddress [] clients() {
        return clients.keySet().toArray(
                new DirectSocketAddress[clients.size()]);
    }

    public synchronized DirectSocketAddress [] hubs() {
        return hubs.keySet().toArray(new DirectSocketAddress[hubs.size()]);
    }
}
