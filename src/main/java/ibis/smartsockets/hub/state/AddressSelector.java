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


public class AddressSelector extends Selector {

    private LinkedList<DirectSocketAddress> result = new LinkedList<DirectSocketAddress>();

    public boolean needAll() {
        return true;
    }

    public void select(HubDescription description) {

        if (description.getConnection() != null) {
            result.add(description.hubAddress);
        }
    }

    public LinkedList<DirectSocketAddress> getResult() {
        return result;
    }
}