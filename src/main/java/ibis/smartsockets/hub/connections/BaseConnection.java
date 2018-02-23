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
package ibis.smartsockets.hub.connections;

import ibis.smartsockets.direct.DirectSocket;
import ibis.smartsockets.direct.DirectSocketAddress;
import ibis.smartsockets.hub.Connections;
import ibis.smartsockets.hub.Statistics;
import ibis.smartsockets.hub.StatisticsCallback;
import ibis.smartsockets.hub.state.HubList;
import ibis.smartsockets.util.ThreadPool;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public abstract class BaseConnection implements Runnable {

    protected final DirectSocket s;
    protected final DataInputStream in;
    protected final DataOutputStream out;

    protected Connections connections;

    protected final HubList knownHubs;

    protected final StatisticsCallback callback;
    protected final long statisticsInterval;

    protected BaseConnection(DirectSocket s, DataInputStream in,
            DataOutputStream out, Connections connections, HubList hubs,
            StatisticsCallback callback, long statisticsInterval) {

        this.s = s;
        this.in = in;
        this.out = out;
        this.connections = connections;
        this.knownHubs = hubs;
        this.statisticsInterval = statisticsInterval;
        this.callback = callback;
    }

    public void activate() {
        ThreadPool.createNew(this, getName());
    }

    public DirectSocketAddress getLocalHub() {
        return knownHubs.getLocalDescription().hubAddress;
    }

    public boolean isLocalHub(DirectSocketAddress sa) {
        // return getLocalHub().equals(sa);
	// This is too strong. Should test for same process. --Ceriel
	return getLocalHub().sameProcess(sa);
    }

    public void run() {

        boolean cont = true;

        long next = System.currentTimeMillis() + statisticsInterval;

        while (cont) {
            cont = runConnection();

            if (System.currentTimeMillis() > next) {

                Statistics s = getStatistics();

                if (s != null && callback != null) {
                    callback.add(s);
                }

                next = System.currentTimeMillis() + statisticsInterval;
            }
        }

        // NOTE: Do NOT close the socket here, since it may still be in use!
    }

    protected abstract boolean runConnection();
    protected abstract String getName();
    protected abstract Statistics getStatistics();

  //  public abstract void printStatistics();

}
