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
package test.virtual.simple;

import ibis.smartsockets.util.MultiplexStreamFactory;
import ibis.smartsockets.virtual.InitializationException;
import ibis.smartsockets.virtual.VirtualServerSocket;
import ibis.smartsockets.virtual.VirtualSocket;
import ibis.smartsockets.virtual.VirtualSocketAddress;
import ibis.smartsockets.virtual.VirtualSocketFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;



public class MultiplexTest1 {

    public static void main(String [] args) throws IOException {

        VirtualSocketFactory sf = null;

        try {
            sf = VirtualSocketFactory.createSocketFactory();
        } catch (InitializationException e1) {
            System.out.println("Failed to create socketfactory!");
            e1.printStackTrace();
            System.exit(1);
        }

        if (args.length > 0) {
            for  (int i=0;i<args.length;i++) {
                VirtualSocketAddress target = new VirtualSocketAddress(args[i]);
                VirtualSocket s = sf.createClientSocket(target, 0, null);

                System.out.println("Created connection to " + target);

                MultiplexStreamFactory f =
                    new MultiplexStreamFactory(s.getInputStream(),
                            s.getOutputStream());

                DataInputStream in = new DataInputStream(f.getBaseIn());
                DataOutputStream out = new DataOutputStream(f.getBaseOut());

                out.writeUTF("Hello server!");
                out.flush();

                out.writeUTF("Hello server!");
                out.flush();

                System.out.println("Server says: " + in.readUTF());

                out.close();
                in.close();

                f.close();
                s.close();
            }
        } else {
            System.out.println("Creating server socket");

            VirtualServerSocket ss = sf.createServerSocket(0, 0, null);

            System.out.println("Created server on " + ss.getLocalSocketAddress());

            while (true) {
                VirtualSocket s = ss.accept();

                System.out.println("Incoming connection from "
                        + s.getRemoteSocketAddress());

                MultiplexStreamFactory f =
                    new MultiplexStreamFactory(s.getInputStream(),
                            s.getOutputStream());

                DataInputStream in = new DataInputStream(f.getBaseIn());
                DataOutputStream out = new DataOutputStream(f.getBaseOut());

                System.out.println("Client says: " + in.readUTF());
                System.out.println("Client says: " + in.readUTF());

                out.writeUTF("Hello client!");

                out.close();
                in.close();

                f.close();
                s.close();
            }
        }
    }
}
