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
package ibis.smartsockets.direct;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.nio.channels.SocketChannel;

public abstract class DirectSocket {

    protected final InputStream in;
    protected final OutputStream out;

    protected final DirectSocketAddress local;
    protected final DirectSocketAddress remote;

    private int userData;

    DirectSocket(DirectSocketAddress local, DirectSocketAddress remote,
            InputStream in, OutputStream out) {

        this.local = local;
        this.remote = remote;

        this.in = in;
        this.out = out;
    }

    public int getUserData() {
        return userData;
    }

    public void setUserData(int userData) {
        this.userData = userData;
    }

    public InputStream getInputStream() throws IOException {
        return in;
    }

    public OutputStream getOutputStream() throws IOException {
        return out;
    }

    public DirectSocketAddress getLocalAddress() {
        return local;
    }

    public DirectSocketAddress getRemoteAddress() {
        return remote;
    }

    public boolean isBound() {
        return true;
    }

    public boolean isConnected() {
        return !isClosed();
    }

    public abstract int getLocalPort() throws IOException;

    public abstract void close() throws IOException;
    public abstract boolean isClosed();

    public abstract void setReceiveBufferSize(int sz) throws SocketException;
    public abstract int getReceiveBufferSize() throws SocketException;

    public abstract void setSendBufferSize(int sz) throws SocketException;
    public abstract int getSendBufferSize() throws SocketException;

    public abstract void setSoTimeout(int t) throws SocketException;
    public abstract int getSoTimeout() throws SocketException;

    public abstract void setTcpNoDelay(boolean on) throws SocketException;
    public abstract boolean getTcpNoDelay() throws SocketException;
    
    public abstract void setKeepAlive(boolean on) throws SocketException;
    public abstract boolean getKeepAlive() throws SocketException;

    public abstract SocketChannel getChannel();

    public abstract void setSoLinger(boolean on, int linger) throws SocketException;
    public abstract int getSoLinger() throws SocketException;

    public abstract void setReuseAddress(boolean on) throws SocketException;
    public abstract boolean getReuseAddress() throws SocketException;

    public abstract void shutdownInput() throws IOException;
    public abstract boolean isInputShutdown();

    public abstract void shutdownOutput() throws IOException;
    public abstract boolean isOutputShutdown();

}
