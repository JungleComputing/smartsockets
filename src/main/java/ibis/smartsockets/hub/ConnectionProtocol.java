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
package ibis.smartsockets.hub;

public interface ConnectionProtocol {

    public static final byte HUB_CONNECT         = 1;
    public static final byte SERVICELINK_CONNECT = 2;

    public static final byte CONNECTION_ACCEPTED = 3;
    public static final byte CONNECTION_REFUSED  = 4;
    public static final byte DISCONNECT          = 5;

    public static final byte PING                = 7;
    public static final byte GET_SPLICE_INFO     = 8;

}
