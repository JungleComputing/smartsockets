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

public class VirtualConnectionIndex {

    // Fix: made sure that it either stays even or uneven. --Ceriel
    // TODO: protect against wrap-around. Just lose the %-operation in
    // nextIndex??? This would at least postpone the wrap-around for quite
    // a while :-)

    private long nextIndex = 0;

    public VirtualConnectionIndex(boolean even) {

        int rnd = (int) Math.round(Math.random() * (Integer.MAX_VALUE-1));

        if (even) {
            if (rnd % 2 != 0) {
                rnd++;
            }
        } else {
            if (!(rnd % 2 != 0)) {
                rnd++;
            }
        }

        nextIndex = rnd % (Integer.MAX_VALUE - 1);
    }

    // Made synchronized --Ceriel
    public synchronized long nextIndex() {
        long result = nextIndex;
        nextIndex = (nextIndex + 2) % (Integer.MAX_VALUE - 1);
        return result;
    }

}
