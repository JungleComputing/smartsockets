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
package ibis.smartsockets.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class FixedSizeHashMap<K,V> extends LinkedHashMap<K,V> {

    private static final long serialVersionUID = 5652104148565673432L;

    private static final int DEFAULT_MAX_ENTRIES = 25;

    private final int MAX_ENTRIES;

    public FixedSizeHashMap() {
        this(DEFAULT_MAX_ENTRIES);
    }

    public FixedSizeHashMap(int size) {
        super();
        MAX_ENTRIES = size;
    }

    public FixedSizeHashMap(int size, int initialCapacity) {
        super(initialCapacity);
        MAX_ENTRIES = size;
    }

    public FixedSizeHashMap(int size, int initialCapacity, float loadfact) {
        super(initialCapacity, loadfact);
        MAX_ENTRIES = size;
    }

    protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
       return size() > MAX_ENTRIES;
    }

}
