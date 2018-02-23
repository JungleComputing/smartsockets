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
package ibis.smartsockets.plugin;

import ibis.smartsockets.virtual.InitializationException;
import ibis.smartsockets.virtual.VirtualSocketFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import javax.net.ServerSocketFactory;


public class SmartServerSocketFactory extends ServerSocketFactory {

    private static SmartServerSocketFactory defaultFactory;

    private VirtualSocketFactory factory;

    private SmartServerSocketFactory() throws InitializationException {
        factory = VirtualSocketFactory.getDefaultSocketFactory();
    }

    @Override
    public ServerSocket createServerSocket() throws IOException {
        return new SmartServerSocket(factory.createServerSocket(null));
    }

    @Override
    public ServerSocket createServerSocket(int port) throws IOException {
        return new SmartServerSocket(factory.createServerSocket(port, 50, null));
    }

    @Override
    public ServerSocket createServerSocket(int port, int backlog)
            throws IOException {
        return new SmartServerSocket(factory.createServerSocket(port, backlog, null));
    }

    @Override
    public ServerSocket createServerSocket(int port, int backlog,
            InetAddress ifAddress) throws IOException {
        throw new IOException("Failed to bind serversocket to " + ifAddress);
    }

    public synchronized static SmartServerSocketFactory getDefault() {

        if (defaultFactory == null) {
            try {
                defaultFactory = new SmartServerSocketFactory();
            } catch (InitializationException e) {
                System.err.println("WARNING: failed to create " +
                        "SmartServerSocketFactory");
                e.printStackTrace(System.err);
                return null;
            }
        }

        return defaultFactory;
    }
}
