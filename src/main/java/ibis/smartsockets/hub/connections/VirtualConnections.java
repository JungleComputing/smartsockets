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

import java.util.HashMap;
import java.util.LinkedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirtualConnections {

    private static Logger vclogger =
        LoggerFactory.getLogger("ibis.smartsockets.hub.connections.virtual");

    private final HashMap<String, VirtualConnection> vcs =
        new HashMap<String, VirtualConnection>();

    public synchronized void register(VirtualConnection vc) {

        if (vclogger.isDebugEnabled()) {
            vclogger.debug("register VC: " + vc);
        }

        vcs.put(vc.key1, vc);
        vcs.put(vc.key2, vc);
    }

    public synchronized VirtualConnection find(String key) {

        if (vclogger.isDebugEnabled()) {
            vclogger.debug("find VC: " + key);
        }

        VirtualConnection vc = vcs.get(key);

        if (vclogger.isInfoEnabled()) {
            vclogger.info("found VC: " + vc);
        }

        return vc;
    }

    public synchronized VirtualConnection remove(String key) {

        VirtualConnection vc = vcs.get(key);

        if (vc == null) {
            // This may happen, since a connection may be simultaneously closed
            // from both sides....
            if (vclogger.isInfoEnabled()) {
                vclogger.info("cannot remove VC: " + key
                        + " since it doesn't exist!");
            }
            return null;
        }

        if (vclogger.isInfoEnabled()) {
            vclogger.info("removing VC: " + vc);
        }

        vcs.remove(vc.key1);
        vcs.remove(vc.key2);

        return vc;
    }

    public synchronized LinkedList<VirtualConnection> removeAll(String prefix) {

        LinkedList<String> remove = new LinkedList<String>();
        LinkedList<VirtualConnection> result = new LinkedList<VirtualConnection>();

        for (String key : vcs.keySet()) {

            if (key.startsWith(prefix)) {
                remove.add(key);
            }
        }

        for (String key : remove) {
            result.add(vcs.remove(key));
        }

        return result;
    }
 }
