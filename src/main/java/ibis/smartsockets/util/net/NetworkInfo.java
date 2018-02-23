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
package ibis.smartsockets.util.net;

import java.net.InetAddress;
import java.util.Arrays;

public class NetworkInfo {

    InetAddress ipv4;
    InetAddress ipv6;

    byte [] netmask;
    byte [] broadcast;
    byte [] mac;

    boolean complete() {
        return (ipv4 != null || ipv6 != null) && netmask != null
            && broadcast != null && mac != null;
    }

    public String toString() {
        return "MAC = " + Arrays.toString(mac)
            + " IPv4 = " + ipv4
            + " Mask = " + Arrays.toString(netmask)
            + " Broadcast = " + Arrays.toString(broadcast)
            + " IPv6 = " + ipv6;
    }
}
