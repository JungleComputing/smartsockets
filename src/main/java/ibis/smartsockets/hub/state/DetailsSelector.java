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

import java.util.LinkedList;

public class DetailsSelector extends Selector {

    private LinkedList<String> result = new LinkedList<String>();

    public boolean needAll() {
        return true;
    }

    public void select(HubDescription description) {

        StringBuffer tmp = new StringBuffer("HubInfo(");

        tmp.append(description.hubAddressAsString);
        tmp.append(",");

        String name = description.getName();

        if (name == null || name.length() == 0) {
            name = "<unknown>";
        }

        tmp.append(name);
        tmp.append(",");
        tmp.append(description.getVizInfo());
        tmp.append(",");
        tmp.append(description.getHomeState());
        tmp.append(",");
        tmp.append(description.numberOfClients());
        tmp.append(",");

        String [] con = description.connectedTo();

        if (con == null) {
            tmp.append("0");
        } else {
            tmp.append(con.length);

            for (int i=0;i<con.length;i++) {
                tmp.append(",");
                tmp.append(con[i]);
            }
        }

        tmp.append(")");

        result.add(tmp.toString());
    }

    public LinkedList<String> getResult() {
        return result;
    }
}
