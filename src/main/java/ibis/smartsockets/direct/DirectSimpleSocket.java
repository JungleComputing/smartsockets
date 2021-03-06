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
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;

public class DirectSimpleSocket extends DirectSocket {

    private final Socket socket;

    DirectSimpleSocket(DirectSocketAddress local, DirectSocketAddress remote,
            InputStream in, OutputStream out, Socket socket) {

        super(local, remote, in, out);

        this.socket = socket;
    }

    /*
    DirectSimpleSocket(SocketAddressSet local, Socket socket) {

        super(local);

        this.socket = socket;
    }*/


    public void close() throws IOException {
        socket.close();
    }

    public SocketChannel getChannel() {
        return socket.getChannel();
    }

    public boolean getKeepAlive() throws SocketException {
        return socket.getKeepAlive();
    }

    public int getReceiveBufferSize() throws SocketException {
        return socket.getReceiveBufferSize();
    }

    public boolean getReuseAddress() throws SocketException {
        return socket.getReuseAddress();
    }

    public int getSendBufferSize() throws SocketException {
        return socket.getSendBufferSize();
    }

    public int getSoLinger() throws SocketException {
        return socket.getSoLinger();
    }

    public int getSoTimeout() throws SocketException {
        return socket.getSoTimeout();
    }

    public boolean getTcpNoDelay() throws SocketException {
        return socket.getTcpNoDelay();
    }

    /*
    public int getTrafficClass() throws SocketException {
        return socket.getTrafficClass();
    }
     */

    public boolean isBound() {
        return socket.isBound();
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

    public boolean isConnected() {
        return socket.isConnected();
    }

    public boolean isInputShutdown() {
        return socket.isInputShutdown();
    }

    public boolean isOutputShutdown() {
        return socket.isOutputShutdown();
    }

    /*
    public void sendUrgentData(int data) throws IOException {
        socket.sendUrgentData(data);
    }
    */

    public void setKeepAlive(boolean on) throws SocketException {
        socket.setKeepAlive(on);
    }

    /*
    public void setOOBInline(boolean on) throws SocketException {
        socket.setOOBInline(on);
    }*/

    public void setReceiveBufferSize(int sz) throws SocketException {
        socket.setReceiveBufferSize(sz);
    }

    public void setReuseAddress(boolean on) throws SocketException {
        socket.setReuseAddress(on);
    }

    public void setSendBufferSize(int sz) throws SocketException {
        socket.setSendBufferSize(sz);
    }

    public void setSoLinger(boolean on, int linger) throws SocketException {
        socket.setSoLinger(on, linger);
    }

    public void setSoTimeout(int t) throws SocketException {
        socket.setSoTimeout(t);
    }

    public void setTcpNoDelay(boolean on) throws SocketException {
        socket.setTcpNoDelay(on);
    }

    /*
    public void setTrafficClass(int tc) throws SocketException {
        socket.setTrafficClass(tc);
    }
     */

    public void shutdownInput() throws IOException {
        socket.shutdownInput();
    }

    public void shutdownOutput() throws IOException {
        socket.shutdownOutput();
    }

    public int getLocalPort() throws IOException {
        return socket.getLocalPort();
    }

    public SocketAddress getRemoteSocketAddress() {
        return socket.getRemoteSocketAddress();
    }

    public SocketAddress getLocalSocketAddress() {
        return socket.getLocalSocketAddress();
    }

    public String toString() {
        return "DirectSimpleSocket(" + local + ")";
    }
}

