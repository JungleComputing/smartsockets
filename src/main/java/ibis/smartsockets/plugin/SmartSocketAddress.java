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
package ibis.smartsockets.plugin;

import ibis.smartsockets.virtual.VirtualSocketAddress;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;


public class SmartSocketAddress {

    public static SocketAddress create(String hostport, boolean smart)
        throws UnknownHostException {

        // First see if InetSocketAddress understands 'hostport'. If so, there
        // is no need using a VirtualSocketAddress.
        if (!smart) {
            int index = hostport.indexOf(':');

            if (index == -1) {
                throw new IllegalArgumentException("String does not contain a "
                        + "InetSocketAddress!");
            }

            try {
                int port = Integer.parseInt(hostport.substring(index+1));
                return new InetSocketAddress(hostport.substring(0, index), port);
            } catch (Exception e) {
                throw new IllegalArgumentException("String does not contain a "
                        + "InetSocketAddress! - cannot parse port!", e);
            }
        } else {
            return new VirtualSocketAddress(hostport);
        }
    }

    public static SocketAddress create(String host, int port, boolean smart)
        throws UnknownHostException {

        // First see if InetSocketAddress understands 'host'. If so, there
        // is no need using a VirtualSocketAddress.
        if (!smart) {
            return new InetSocketAddress(host, port);
        } else {
            return new VirtualSocketAddress(host, port);
        }
    }
}
