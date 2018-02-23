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
package test.virtual.address;

import ibis.smartsockets.virtual.VirtualServerSocket;
import ibis.smartsockets.virtual.VirtualSocketAddress;
import ibis.smartsockets.virtual.VirtualSocketFactory;

public class AddressSize {

    public static void main(String [] args) {

        try {
            VirtualSocketFactory f = VirtualSocketFactory.createSocketFactory();
            VirtualServerSocket s = f.createServerSocket(8899, 1, null);
            VirtualSocketAddress a = s.getLocalSocketAddress();

            System.out.println("Address  : " + a);
            System.out.println("Codedform: " + a.toBytes().length);

        } catch (Exception e) {
            System.err.println("Oops: " + e);
        }
    }

}
