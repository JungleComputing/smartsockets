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

import ibis.smartsockets.hub.connections.HubConnection;

import java.util.LinkedList;


public class ConnectionsSelector extends Selector {

    private LinkedList<HubConnection> result = new LinkedList<HubConnection>();

    public boolean needAll() {
        return true;
    }

    public void select(HubDescription description) {

        HubConnection c = description.getConnection();

        if (c != null) {
            result.add(c);
        }
    }

    public LinkedList<HubConnection> getResult() {
        return result;
    }
}
