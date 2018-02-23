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

class Receiver extends Thread {

    private final DataSink d;
    private final VirtualSocket s;
    private final DataInputStream in;
    private final DataOutputStream out;

    private final byte [] data;

    Receiver(DataSink d, VirtualSocket s, DataOutputStream out,
            DataInputStream in, int size) {

        this.d = d;
        this.s = s;
        this.out = out;
        this.in = in;
        data = new byte[size];
    }

    private boolean receiveData() {

        int block = -2;

        try {
            do {
                block = in.readInt();

                //System.out.println("Got block " + block);

                if (block >= 0) {
                    in.readFully(data);
                }
            } while (block >= 0);
        } catch (Exception e) {
            System.out.println("Failed to read data!" + e);
        }

        // TODO: do something with stats ?
        d.done();

        return (block == -2);
    }

    public void run() {

        boolean stop = false;

        while (!stop) {
            stop = receiveData();
        }

        VirtualSocketFactory.close(s, out, in);
    }
}
