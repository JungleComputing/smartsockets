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
package ibis.smartsockets.virtual;

import ibis.smartsockets.direct.IPAddressSet;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.channels.ServerSocketChannel;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;


public class VirtualServerSocket {

    private final VirtualSocketFactory parent;
    private int port;

    private final LinkedList<VirtualSocket> incoming =
        new LinkedList<VirtualSocket>();

    private int backlog;

    private final int defaultTimeout;
    private int timeout = 0;
    private boolean reuseAddress = true;
    private boolean closed = false;

    private VirtualSocketAddress localAddress;

    private Map<String, Object> properties;

    private boolean bound;
    private int receiveBufferSize = -1;

    // Create unbound port
    protected VirtualServerSocket(VirtualSocketFactory parent,
            int defaultTimeout, Map<String, Object> p) {
        this.parent = parent;
        this.properties = p;
        this.bound = false;
        this.defaultTimeout = defaultTimeout;
    }

    // Create bound port
    protected VirtualServerSocket(VirtualSocketFactory parent,
            VirtualSocketAddress address, int port, int backlog,
            int defaultTimeout, Map<String, Object> p) {

        this.parent = parent;
        this.port = port;
        this.backlog = backlog;
        this.localAddress = address;
        this.properties = p;
        this.bound = true;
        this.defaultTimeout = defaultTimeout;
    }

    public synchronized int incomingConnection(VirtualSocket s) {

	if (VirtualSocketFactory.conlogger.isDebugEnabled()) {
	    VirtualSocketFactory.conlogger.debug("Got connection " + s);
	}
        if (closed) {
            if (VirtualSocketFactory.conlogger.isDebugEnabled()) {
        	VirtualSocketFactory.conlogger.debug("But the server socket was closed");
            }
            return -1;
        }

        if (incoming.size() < backlog) {
            incoming.addLast(s);
            notifyAll();
            return 0;
        }
  
        // Try to remove all closed sockets from the queue ...
        ListIterator<VirtualSocket> itt = incoming.listIterator();

        while (itt.hasNext()) {
            VirtualSocket v = itt.next();

            if (v.isClosed()) {
                itt.remove();
            }
        }

        // See if there is room now...
        if (incoming.size() < backlog) {
            incoming.addLast(s);
            notifyAll();
            return 0;
        }

        // If not, print an error.....
        if (VirtualSocketFactory.conlogger.isInfoEnabled()) {
            VirtualSocketFactory.conlogger.info("Incoming connection on port "
                    + port + " refused: QUEUE FULL (" + incoming.size() + ", "
                    + System.currentTimeMillis() + ")");
        }

        return 1;
    }

    private synchronized VirtualSocket getConnection()
        throws SocketTimeoutException {

        while (incoming.size() == 0 && !closed) {
            try {
                wait(timeout);
            } catch (Exception e) {
                // ignore
            }

            // Check if our wait time has expired.
            if (timeout > 0 && incoming.size() == 0 && !closed) {
                throw new SocketTimeoutException("Time out during accept");
            }
        }

        if (incoming.size() > 0) {
            return incoming.removeFirst();
        } else {
            return null;
        }
    }

    public VirtualSocket accept() throws IOException {

        VirtualSocket result = null;

        while (result == null) {
            result = getConnection();
            if (VirtualSocketFactory.logger.isDebugEnabled()) {
        	VirtualSocketFactory.logger.debug("VirtualServerPort got connection");
            }
            if (result == null) {
        	if (VirtualSocketFactory.logger.isDebugEnabled()) {
        	    VirtualSocketFactory.logger.debug("closed during accept");
                }
                // Can only happen if socket has been closed
                throw new IOException("Socket closed during accept");
            } else if (result.isClosed()) {
        	if (VirtualSocketFactory.logger.isDebugEnabled()) {
        	    VirtualSocketFactory.logger.debug("other side already closed");
                }
                // Check if the other side is already closed...
                result = null;
            } else {
                // See if the other side is still willing to connect ...
                try {
                    int t = timeout;

                    if (timeout <= 0) {
                        t = defaultTimeout;
                    }
                    if (VirtualSocketFactory.logger.isDebugEnabled()) {
                        VirtualSocketFactory.logger.debug("timeout = " + timeout);
                    }

                    result.connectionAccepted(t);
                } catch (IOException e) {
                    if (VirtualSocketFactory.logger.isInfoEnabled()) {
                        VirtualSocketFactory.logger.info("VirtualServerPort( "
                                + port + ") got exception during accept!", e);
                    }
                    result = null;
                }
            }
        }

        result.setTcpNoDelay(true);
        return result;
    }

    public synchronized void close() throws IOException {

        closed = true;
        notifyAll();   // wakes up any waiting accept

        while (incoming.size() != 0) {
            incoming.removeFirst().connectionRejected(1000);
        }

        parent.closed(port);
    }

    public int getPort() {
        return port;
    }

    public boolean isClosed() {
        return closed;
    }

    public ServerSocketChannel getChannel() {
        throw new RuntimeException("operation not implemented by " + this);
    }

    public IPAddressSet getIbisInetAddress() {
        throw new RuntimeException("operation not implemented by " + this);
    }

    public VirtualSocketAddress getLocalSocketAddress() {
        return localAddress;
    }

    public int getSoTimeout() throws IOException {
        return timeout;
    }

    public void setSoTimeout(int t) throws SocketException {
	if (VirtualSocketFactory.conlogger.isDebugEnabled()) {
	    VirtualSocketFactory.conlogger.debug("setSoTimeout " + t, new Throwable());
	}
        timeout = t;
    }

    public boolean getReuseAddress() throws SocketException {
        return reuseAddress;
    }

    public void setReuseAddress(boolean v) throws SocketException {
        reuseAddress = v;
    }

    public String toString() {
        if (localAddress == null) {
            return "VirtualServerSocket(UNBOUND)";
        }
        return "VirtualServerSocket(" + localAddress.toString() + ")";
    }

    public Map<?, ?> properties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public void setProperty(String key, Object val) {
        properties.put(key, val);
    }

    public boolean isBound() {
        return bound;
    }

    public void setReceiveBufferSize(int size) {
        // TODO: Find a way to this in the bind operation ?
        receiveBufferSize = size;
    }

    public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
        // not implemented
    }

    public int getReceiveBufferSize() {
        return receiveBufferSize;
    }

    public int getLocalPort() {
        return port;
    }

    public InetAddress getInetAddress() {
        // TODO Auto-generated method stub
        return null;
    }

    public void bind(SocketAddress endpoint, int backlog) throws IOException {

        if (endpoint instanceof VirtualSocketAddress) {

            int tmp = ((VirtualSocketAddress) endpoint).port();

            parent.bindServerSocket(this, tmp);

            this.port = tmp;
            this.localAddress = ((VirtualSocketAddress) endpoint);
        } else {
            throw new IOException("Unsupported address type");
        }
    }
}
