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
package test.virtual.alltoall;


import ibis.smartsockets.virtual.VirtualServerSocket;
import ibis.smartsockets.virtual.VirtualSocket;
import ibis.smartsockets.virtual.VirtualSocketAddress;
import ibis.smartsockets.virtual.VirtualSocketFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class Server {

    private static final int DEFAULT_PORT = 5689;

    private static class Client {

        final VirtualSocketAddress address;

        final int number;
        final VirtualSocket s;
        final DataInputStream in;
        final DataOutputStream out;

        Client(VirtualSocket s, int number) throws IOException {
            this.s = s;
            this.number = number;
            in = new DataInputStream(s.getInputStream());
            out = new DataOutputStream(s.getOutputStream());
            address = new VirtualSocketAddress(in.readUTF());
        }

        void writeClients(Client[] clients) throws IOException {

            out.writeInt(number);
            out.writeInt(clients.length);

            for (int i=0;i<clients.length;i++) {
                out.writeUTF(clients[i].address.toString());
            }

            out.flush();
        }

        void close() {

            try {
                out.close();
            } catch (Exception e) {
                // ignore
            }

            try {
                in.close();
            } catch (Exception e) {
                // ignore
            }

            try {
                s.close();
            } catch (Exception e) {
                // ignore
            }
        }

        public String toString() {
            return number + " -> " + address.toString();
        }
    }

    public static void main(String[] args) {

        if (args.length != 1) {
            System.err.println("Usage java test.alltoall.Server <#clients>");
            System.exit(1);
        }

        int machines = Integer.parseInt(args[0]);

        System.out.println("Expecting " + machines + " clients");

        Client [] clients = new Client[machines];

        try {
            VirtualSocketFactory factory =
                VirtualSocketFactory.createSocketFactory();
            VirtualServerSocket ss = factory.createServerSocket(DEFAULT_PORT,
                    50, null);

            System.out.println("Created server socket at " + ss);

            int numclients = 0;

            while (numclients < machines) {
                System.out.println("Waiting for clients (" + numclients + "/"
                        + machines + ")");

                VirtualSocket s = ss.accept();

                System.out.println("Got client " + numclients);

                clients[numclients] = new Client(s, numclients);
                numclients++;
            }

            System.out.println("Got all clients: ");

            for (int i=0;i<clients.length;i++) {
                System.out.println(clients[i].toString());
            }

            System.out.println();

            for (int i=0;i<clients.length;i++) {
                System.out.println("Writing reply to " + i);
                clients[i].writeClients(clients);
                clients[i].close();
            }

            System.out.println("Done");

        } catch (Exception e) {
            System.out.println("Server got exception " + e);
            e.printStackTrace(System.err);
        }
    }
}
