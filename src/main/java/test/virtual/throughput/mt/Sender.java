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
/**
 *
 */
package test.virtual.throughput.mt;

import ibis.smartsockets.virtual.VirtualSocket;
import ibis.smartsockets.virtual.VirtualSocketFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;

class Sender extends Thread {

    private final DataSource d;
    private final VirtualSocket s;
    private final DataInputStream in;
    private final DataOutputStream out;
    private final byte [] data;

    Sender(DataSource d, VirtualSocket s, DataOutputStream out,
            DataInputStream in, int size) {

        this.d = d;
        this.s = s;
        this.out = out;
        this.in = in;
        this.data = new byte[size];
    }

    private void sendData() {

        //System.out.println("Sending data!");

       // long time = System.currentTimeMillis();

        // int count = 0;
        int block = d.getBlock();

        while (block != -1) {
            // count++;

            try {
                out.writeInt(block);
                out.write(data);
                out.flush();
            } catch (Exception e) {
                System.out.println("Failed to write data!" + e);
            }

            block = d.getBlock();
        }

        try {
            out.writeInt(-1);
            out.flush();
        } catch (Exception e) {
            System.out.println("Failed to write data!" + e);
        }

        // time = System.currentTimeMillis() - time;

        // TODO: do something with stats!
    }

    public void run() {

        boolean done = d.waitForStartOrDone();

        while (!done) {
            sendData();
            done = d.waitForStartOrDone();
        }

        try {
            out.writeInt(-2);
            out.flush();
        } catch (Exception e) {
            System.out.println("Failed to write data!" + e);
        }

        VirtualSocketFactory.close(s, out, in);
    }
}
