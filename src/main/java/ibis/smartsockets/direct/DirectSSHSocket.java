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

import ibis.smartsockets.util.ssh.LocalStreamForwarder;

// import ch.ethz.ssh2.LocalStreamForwarder;

// import com.trilead.ssh2.LocalStreamForwarder;

public class DirectSSHSocket extends DirectSocket {

    private final LocalStreamForwarder lsf;
    private boolean closed = false;

    public DirectSSHSocket(DirectSocketAddress local, DirectSocketAddress remote, InputStream in, OutputStream out, LocalStreamForwarder lsf) {

        super(local, remote, in, out);

        this.lsf = lsf;
    }

    public void close() throws IOException {
        lsf.close();
        closed = true;
    }

    @Override
    public SocketChannel getChannel() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getReceiveBufferSize() throws SocketException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean getReuseAddress() throws SocketException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getSendBufferSize() throws SocketException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getSoLinger() throws SocketException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getSoTimeout() throws SocketException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean getTcpNoDelay() throws SocketException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public boolean isInputShutdown() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isOutputShutdown() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setReceiveBufferSize(int sz) throws SocketException {
        // TODO Auto-generated method stub
    }

    @Override
    public void setReuseAddress(boolean on) throws SocketException {
        // TODO Auto-generated method stub
    }

    @Override
    public void setSendBufferSize(int sz) throws SocketException {
        // TODO Auto-generated method stub
    }

    @Override
    public void setSoLinger(boolean on, int linger) throws SocketException {
        // TODO Auto-generated method stub
    }

    @Override
    public void setSoTimeout(int t) throws SocketException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setTcpNoDelay(boolean on) throws SocketException {
        // TODO Auto-generated method stub
    }

    @Override
    public void shutdownInput() throws IOException {
        // TODO Auto-generated method stub
    }

    @Override
    public void shutdownOutput() throws IOException {
        // TODO Auto-generated method stub
    }

    @Override
    public int getLocalPort() throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setKeepAlive(boolean on) throws SocketException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean getKeepAlive() throws SocketException {
        // TODO Auto-generated method stub
        return false;
    }
}
