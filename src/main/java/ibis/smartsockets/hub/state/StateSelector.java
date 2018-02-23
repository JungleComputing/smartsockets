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

public class StateSelector extends Selector {

    private LinkedList<HubDescription> result = new LinkedList<HubDescription>();
    private final long state;

    public StateSelector(long state) {
        this.state = state;
    }

    public boolean needAll() {
        return true;
    }

    public void select(HubDescription description) {
        if (description.getLastLocalUpdate() > state) {
            result.add(description);
        }
    }

    public LinkedList<HubDescription> getResult() {
        return result;
    }
}
