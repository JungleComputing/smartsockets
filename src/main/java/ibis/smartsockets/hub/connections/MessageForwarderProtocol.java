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

import ibis.smartsockets.hub.ConnectionProtocol;

public interface MessageForwarderProtocol extends ConnectionProtocol {

    public static final byte CREATE_VIRTUAL          = 60;
    public static final byte CREATE_VIRTUAL_ACK      = 61;
    public static final byte CREATE_VIRTUAL_NACK     = 62;
    public static final byte CREATE_VIRTUAL_ACK_ACK  = 63;

    public static final byte CLOSE_VIRTUAL           = 64;

    public static final byte MESSAGE_VIRTUAL         = 65;
    public static final byte MESSAGE_VIRTUAL_ACK     = 66;

    public static final byte DATA_MESSAGE            = 68;
    public static final byte INFO_MESSAGE            = 69;

}
