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
package ibis.smartsockets.hub.servicelink;

import ibis.smartsockets.direct.DirectSocketAddress;

public class HubInfo {

    public final DirectSocketAddress hubAddress;
    public final String name;
    public final long state;
    public final int clients;

    public final String vizInfo;


    public final DirectSocketAddress [] connectedTo;
    public final boolean [] usingSSH;

    public HubInfo(String info) {

        if (!info.startsWith("HubInfo(") || !info.endsWith(")")) {
            throw new IllegalArgumentException("String does not contain " +
                    "HubInfo!");
        }

        try {

            String[] strings = info.substring(8, info.length()-1).split(",");

//            StringTokenizer t =
//                new StringTokenizer(info.substring(8, info.length()-1), ",");

            hubAddress = DirectSocketAddress.getByAddress(strings[0]);
            name = strings[1];
            vizInfo = strings[2];
            state = Long.parseLong(strings[3]);
            clients = Integer.parseInt(strings[4]);

            int tmp = Integer.parseInt(strings[5]);

            connectedTo = new DirectSocketAddress[tmp];
            usingSSH = new boolean[tmp];

            for (int i=0;i<connectedTo.length;i++) {

                String address = strings[6 + i];

                if (address.endsWith(" (SSH)")) {
                    address = address.substring(0, address.length()-6);
                    usingSSH[i] = true;
                } else {
                    usingSSH[i] = false;
                }

                connectedTo[i] = DirectSocketAddress.getByAddress(address);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("String does not contain HubInfo"
                    + ": \"" + info + "\"", e);
        }
    }



}


