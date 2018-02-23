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
package ibis.smartsockets.viz;

import ibis.smartsockets.hub.servicelink.ClientInfo;

import java.awt.Color;


import com.touchgraph.graphlayout.Node;

public class NameServerClientNode extends ClientNode {

    public NameServerClientNode(ClientInfo info, HubNode hub) {

        super(info.getClientAddress().toString(), hub, false);
        setType(Node.TYPE_CIRCLE);
        setRank(0);

        String adr = info.getClientAddress().toString();

       // System.out.println("Adding NameServer " + adr);

        setMouseOverText(new String[] { "Nameserver:", adr });

        setBackColor(Color.decode("#808080"));
        setNodeBorderInactiveColor(Color.decode("#545454"));
        setLabel("N");
    }
}
