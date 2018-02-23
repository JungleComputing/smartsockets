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

import java.io.DataInputStream;
import java.io.IOException;


public interface VirtualConnectionCallBack {

    void connect(DirectSocketAddress src, DirectSocketAddress sourceHub, int port,
            int fragment, int buffer, int timeout, long index);

    void connectACK(long index, int fragment, int buffer);
    void connectNACK(long index, byte reason);
    void connectACKACK(long index, boolean succes);

    void disconnect(long index);

    boolean gotMessage(long index, int len, DataInputStream in) throws IOException ;
    void gotMessageACK(long index, int data);

}
