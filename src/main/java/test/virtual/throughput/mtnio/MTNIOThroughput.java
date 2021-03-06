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
package test.virtual.throughput.mtnio;

import ibis.smartsockets.virtual.InitializationException;
import ibis.smartsockets.virtual.VirtualServerSocket;
import ibis.smartsockets.virtual.VirtualSocket;
import ibis.smartsockets.virtual.VirtualSocketAddress;
import ibis.smartsockets.virtual.VirtualSocketFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;

public class MTNIOThroughput {

    private static int OPCODE_META = 42;
    private static int OPCODE_DATA = 24;

    private static int DEFAULT_STREAMS = 2;
    private static int DEFAULT_REPEAT = 10;
    private static int DEFAULT_COUNT = -1;

    private static int TIMEOUT = 15000;
    private static int DEFAULT_SIZE = 64*1024;

    private static long DEFAULT_TOTAL_SIZE = 1024L*1024L*1024L;

    private static long total = DEFAULT_TOTAL_SIZE;
    private static int count = DEFAULT_COUNT;
    private static int repeat = DEFAULT_REPEAT;
    private static int size = DEFAULT_SIZE;
    private static int streams = DEFAULT_STREAMS;

    private static VirtualSocketFactory sf;
    private static HashMap<String, Object> connectProperties;

    private static void configure(VirtualSocket s) throws SocketException {

        s.setTcpNoDelay(true);

        System.out.println("Configured socket: ");
        System.out.println(" sendbuffer     = " + s.getSendBufferSize());
        System.out.println(" receiverbuffer = " + s.getReceiveBufferSize());
        System.out.println(" no delay       = " + s.getTcpNoDelay());
    }

    private static void createStreamOut(VirtualSocketAddress target, DataSource d, int id, int size) {

        try {
            VirtualSocket s = sf.createClientSocket(target, TIMEOUT,
                    connectProperties);

            System.out.println("Created DATA connection to " + target);

            configure(s);

            DataInputStream in = new DataInputStream(s.getInputStream());
            DataOutputStream out = new DataOutputStream(s.getOutputStream());

            out.writeInt(OPCODE_DATA);
            out.writeInt(id);
            out.flush();

            new Sender(d, s, out, in, size).start();

        } catch (IOException e) {
            throw new Error("Failed to create connection to " + target, e);
        }
    }

    private static double performance(long time, long size) {

        double tp = (1000.0 * size) / (1024.0*1024.0*time);
        double mbit = (8000.0 * size) / (1024.0*1024.0*time);

        if (mbit > 1000) {
            double tmp = mbit / 1024.0;
            System.out.printf("Test took %d ms. Througput = %4.1f " +
                    "MByte/s (%3.1f GBit/s)\n", time, tp, tmp);
        } else {
            System.out.printf("Test took %d ms. Througput = %4.1f " +
                    "MByte/s (%3.1f MBit/s)\n", time, tp, mbit);
        }

        return mbit;
    }

    public static double [] client(VirtualSocketAddress target) {

        try {
            System.out.println("Starting test: " + size + " " + count + " " + streams);

            VirtualSocket s = sf.createClientSocket(target, TIMEOUT,
                    connectProperties);

            System.out.println("Created META connection to " + target);

            configure(s);

            DataInputStream in = new DataInputStream(s.getInputStream());
            DataOutputStream out = new DataOutputStream(s.getOutputStream());

            // TODO: draw random ID here!
            int id = 42;

            out.writeInt(OPCODE_META);
            out.writeInt(size);
            out.writeInt(count);
            out.writeInt(repeat);
            out.writeInt(streams);
            out.writeInt(id);
            out.flush();

            double [] results = new double[repeat];

            DataSource d = new DataSource();

            for (int i=0;i<streams;i++) {
                createStreamOut(target, d, id, size);
            }

            System.out.println("Starting test");

            for (int r=0;r<repeat;r++) {

                long start = System.currentTimeMillis();

                d.set(count);
                in.readInt();

                long end = System.currentTimeMillis();

                results[r] = performance(end-start, ((long) count)*((long)size));

                // System.out.println("Send " + count + " (" + tmp + ")");

                // TODO: print some stats here!
            }

            d.done();

            VirtualSocketFactory.close(s, out, in);

            return results;

        } catch (Exception e) {
            throw new Error("Failed to create connection to " + target, e);
        }
    }


    private static void createStreamIn(VirtualServerSocket ss, DataSink d,
            int id, int size) {

        try {
            VirtualSocket s = ss.accept();

            System.out.println("Incoming connection from "
                    + s.getRemoteSocketAddress());

            configure(s);

            DataInputStream in = new DataInputStream(s.getInputStream());
            DataOutputStream out = new DataOutputStream(s.getOutputStream());

            int opcode = in.readInt();

            if (opcode != OPCODE_DATA) {
                throw new Error("EEK: sender out of sync (2)!");
            }

            int tmp = in.readInt();

            if (tmp != id) {
                throw new Error("EEK: sender out of sync (3)!");
            }

            new Receiver(d, s, out, in, size).start();

        } catch (Exception e) {
            throw new Error("EEK: got exception while accepting!", e);
        }
    }

    public static void server() throws IOException {

        System.out.println("Creating server");

        VirtualServerSocket ss = sf.createServerSocket(0, 0, connectProperties);

        System.out.println("Created server on " + ss.getLocalSocketAddress());

        while (true) {
            System.out.println("Server waiting for connections");

            VirtualSocket s = null;
            DataInputStream in = null;
            DataOutputStream out = null;

            try {
                s = ss.accept();

                System.out.println("Incoming connection from "
                        + s.getRemoteSocketAddress());

                configure(s);

                in = new DataInputStream(s.getInputStream());
                out = new DataOutputStream(s.getOutputStream());

                int opcode = in.readInt();

                if (opcode != OPCODE_META) {
                    throw new Error("EEK: sender out of sync!");
                }

                size = in.readInt();
                count = in.readInt();
                repeat = in.readInt();
                streams = in.readInt();

                int id = in.readInt();

                DataSink d = new DataSink();

                for (int i=0;i<streams;i++) {
                    createStreamIn(ss, d, id, size);
                }

                for (int r=0;r<repeat;r++) {

                    d.waitForCount(streams);
                    out.writeInt(streams);
                    out.flush();
                }

                System.out.println("done!");
            } catch (Exception e) {
                throw new Error("Server got exception ", e);
            } finally {
                VirtualSocketFactory.close(s, out, in);
            }
        }
    }

    private static void printResult(double [] result, int size) {

        double sum = 0;
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;

        for (int i=0;i<result.length;i++) {

            if (result[i] < min) {
                min = result[i];
            }

            if (result[i] > max) {
                max = result[i];
            }

            sum += result[i];
        }

        double avg = sum / result.length;

        System.out.println("*** " + avg + " " + max + " " + min);
    }

    public static void main(String [] args) throws IOException {

        connectProperties = new HashMap<String, Object>();

        VirtualSocketAddress target = null;

        for (int i=0;i<args.length;i++) {
            if (args[i].equals("-target")) {
                target = new VirtualSocketAddress(args[++i]);
            } else if (args[i].equals("-size")) {
                size = Integer.parseInt(args[++i]);
            } else if (args[i].equals("-total")) {
                total = Integer.parseInt(args[++i]);
            } else if (args[i].equals("-count")) {
                count = Integer.parseInt(args[++i]);
            } else if (args[i].equals("-repeat")) {
                repeat = Integer.parseInt(args[++i]);
            } else if (args[i].equals("-streams")) {
                streams = Integer.parseInt(args[++i]);
            } else if (args[i].equals("-buffers")) {
                int size = Integer.parseInt(args[++i]);
                connectProperties.put("sendbuffer", size);
                connectProperties.put("receivebuffer", size);
            } else {
                System.err.println("Unknown option: " + args[i]);
            }
        }

        try {
            sf = VirtualSocketFactory.createSocketFactory(connectProperties, true);
        } catch (InitializationException e) {
            throw new Error("Failed to create socketfactory!", e);
        }

        if (target == null) {
            server();
        } else {

            if (size > 0) {
                if (count == -1) {
                    count = (int) (total / size);
                }

                client(target);
            } else {
                // variable sizes
                for (int i=1024;i<=256*1024;i*=2) {
                    size = i;
                    count = (int) (total / size);

                    double [] result = client(target);

                    printResult(result, i);
                }
            }
        }
    }
}
