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

import ibis.smartsockets.virtual.VirtualSocketAddress;

public class ToBytes {

    public static void main(String [] args) {

        try {
            String serverString = "130.37.193.29-5555:3210";

            VirtualSocketAddress one = new VirtualSocketAddress(serverString);

            VirtualSocketAddress.fromBytes(one.toBytes(), 0);

        } catch (Exception e) {
            System.out.println("Oops: " + e);
            e.printStackTrace();
        }
    }

}
