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
package ibis.smartsockets.virtual.modules.reverse;

import ibis.smartsockets.SmartSocketsProperties;
import ibis.smartsockets.direct.DirectSocketAddress;
import ibis.smartsockets.util.TypedProperties;
import ibis.smartsockets.virtual.NonFatalIOException;
import ibis.smartsockets.virtual.VirtualServerSocket;
import ibis.smartsockets.virtual.VirtualSocket;
import ibis.smartsockets.virtual.VirtualSocketAddress;
import ibis.smartsockets.virtual.modules.AbstractDirectModule;
import ibis.smartsockets.virtual.modules.MessagingModule;
import ibis.smartsockets.virtual.modules.direct.Direct;
import ibis.smartsockets.virtual.modules.direct.DirectVirtualSocket;

import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class Reverse extends MessagingModule {

    private static final int DEFAULT_CONNECT_TIMEOUT = 3500;
    private static final boolean USE_THREAD = true;

    private static final int PLEASE_CONNECT = 1;
    private static final int CANNOT_CONNECT = 3;

    private Direct direct;
    private int requestID = 0;

    private boolean denyConnectionsToSelf = true;

    private HashMap<Integer, String> replies = new HashMap<Integer, String>();
    private int acceptTimeout;
    private int defaultConnectTimeout;

    private class Connector extends Thread {

        private final VirtualServerSocket ss;
        private final VirtualSocketAddress target;
        private final int timeout;
        private final int requestID;

        Connector(VirtualServerSocket ss, VirtualSocketAddress target,
                int timeout, int requestID) {

            this.ss = ss;
            this.target = target;
            this.timeout = timeout;
            this.requestID = requestID;
        }

        public void run() {
            setupConnection(ss, target, timeout, requestID);
        }
    }

    public Reverse() {
        super("ConnectModule(Reverse)", true);
        properties.put("direct.forcePublic", "");
    }

    public void initModule(TypedProperties properties) throws Exception {

        // This property is a bit backwards, but it is easier to understand
        // for the user in this way.
        denyConnectionsToSelf = !properties.booleanProperty(
                SmartSocketsProperties.REVERSE_CONNECT_SELF, false);
        acceptTimeout = properties.getIntProperty(SmartSocketsProperties.REVERSE_ACCEPT_TIMEOUT, 100);
        defaultConnectTimeout = properties.getIntProperty(SmartSocketsProperties.REVERSE_CONNECT_TIMEOUT, DEFAULT_CONNECT_TIMEOUT);
    }

    public void startModule() throws Exception {
        if (serviceLink == null) {
            throw new Exception(module + ": no service link available!");
        }

        direct = (Direct) parent.findModule("ConnectModule(Direct)");

        if (direct == null) {
            throw new Exception(module + ": no direct module available!");
        }
    }


    public DirectSocketAddress getAddresses() {
        // Nothing to do here....
        return null;
    }

    private void storeReply(int requestID, String reply) {

        if (logger.isDebugEnabled()) {
            logger.debug("Storing reply: [" + requestID + "] " + reply);
        }

        synchronized (replies) {
            if (replies.containsKey(requestID)) {
                replies.put(requestID, reply);
                replies.notifyAll();
            }
        }
    }

    private void storeRequest(int requestID) {

        if (logger.isDebugEnabled()) {
            logger.debug("Storing request: [" + requestID + "]");
        }

        synchronized (replies) {
            replies.put(requestID, null);
        }
    }

    private String removeRequest(int requestID) {

        String result = null;

        synchronized (replies) {
            result = replies.remove(requestID);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Removing request: [" + requestID + "] " + result);
        }

        return result;
    }

    private boolean haveReply(int requestID) {

        String result = null;

        synchronized (replies) {
            result = replies.get(requestID);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Check request: [" + requestID + "] " + result);
        }
        return (result != null);
    }

    private synchronized int nextRequestID() {
        return requestID++;
    }

    public VirtualSocket connect(VirtualSocketAddress target, int timeout,
            Map<String, Object> properties) throws NonFatalIOException {

        // When the reverse module is asked for a connection to a remote
        // address, it simply creates a local serversocket and send a message
        // to the remote machine asking for a connection. If no connection comes
        // in within the specified timeout, the module assumes the connection
        // setup has failed and throws an exception. If a connection does come
        // in, the local socket still has to wait for the remote serversocket to
        // do an accept.

        // First check if we are trying to connect to ourselves (which makes no
        // sense for this module...

        if (denyConnectionsToSelf &&
                target.machine().sameMachine(parent.getLocalHost())) {

            throw new NonFatalIOException("Cannot set up a connection " +
                    "to myself!");
        }


        if (timeout <= 0) {
            timeout = defaultConnectTimeout;
        }

        VirtualServerSocket ss = null;

        try {
            ss = parent.createServerSocket(0, 1, null);
        } catch (Exception e) {
            // All exceptions are converted into a module not suitable
            // exception.
            throw new NonFatalIOException("Failed to set up " +
                    "reverse connection", e);
        }

        int id = nextRequestID();
        String reply = "Attempt timed out";
        DirectVirtualSocket s = null;

        try {
            storeRequest(id);

            byte [][] message = new byte[7][];

            VirtualSocketAddress vs = ss.getLocalSocketAddress();

            message[0] = fromInt(target.port());
            message[1] = fromSocketAddressSet(vs.machine());
            message[2] = fromInt(vs.port());
            message[3] = fromSocketAddressSet(vs.hub());
            message[4] = fromString(vs.cluster());
            message[5] = fromInt(timeout);
            message[6] = fromInt(id);

            serviceLink.send(target.machine(), target.hub(), module,
                    PLEASE_CONNECT, message);

            // Now wait for an incoming connection, while also checking for
            // a reply message to come in...
            int waittime = acceptTimeout;
            boolean stop = false;

            long deadline = System.currentTimeMillis() + timeout;

            while (!stop) {

                long left = deadline - System.currentTimeMillis();

                if (left <= 0) {
                    stop = true;
                } else {

                    if (left < waittime) {
                        waittime = (int) left;
                    }

                    try {
                        ss.setSoTimeout(waittime);
                        s = (DirectVirtualSocket) ss.accept();
                        stop = true;
                    } catch (SocketTimeoutException e) {
                        // ignore
                    }
                }

                if (!stop && haveReply(id)) {
                    stop = true;
                    reply = removeRequest(id);
                }
            }
        } catch (Exception e) {
            // All exceptions are converted into a module not suitable
            // exception.
            throw new NonFatalIOException("Failed to set up reverse connection",
                    e);
        } finally {
            // Always remove the request from the map and close the
            // serversocket!
            removeRequest(id);

            try {
                ss.close();
            } catch (Exception e) {
                // ignored
            }
        }

        if (s == null) {
            throw new NonFatalIOException("Target failed to "
                    + "set up reverse connection (" + reply + ")");
        }

        // The rest of the connection setup is handled by the generic code in
        // the virtual socket factory.
        return s;
    }

    private void sendReply(VirtualSocketAddress to, int requestID, String reply) {

        if (reply == null) {
            reply = "";
        }

        byte [][] message = new byte[2][];

        message[0] = fromInt(requestID);
        message[1] = fromString(reply);

        serviceLink.send(to.machine(), to.hub(), module, CANNOT_CONNECT,
                message);
    }

    void setupConnection(VirtualServerSocket ss, VirtualSocketAddress target,
            int timeout, int requestID) {

        try {
            DirectVirtualSocket s = (DirectVirtualSocket) direct.connect(target,
                    timeout, properties);

            if (logger.isInfoEnabled()) {
                logger.info(module + ": Connection to " + target + " created!");
            }

            // NOTE: The socket is now accepted by the temporary serversocket
            // we created on the other side. Now we must check if the server
            // socket on our side is also willing to accept it.

            ReverseVirtualSocket rvs = new ReverseVirtualSocket(s);

            int accept = ss.incomingConnection(rvs);

            if (accept != 0) {

                if (accept == -1) {

                    if (logger.isInfoEnabled()) {
                        logger.info(module + ": Serversocket is NOT willing " +
                                "to accept (REJECTED)");
                    }

                    s.connectionRejected(AbstractDirectModule.CONNECTION_REJECTED);
                } else {

                    if (logger.isInfoEnabled()) {
                        logger.info(module + ": Serversocket is NOT willing " +
                                "to accept (OVERLOAD)");
                    }

                    s.connectionRejected(AbstractDirectModule.SERVER_OVERLOAD);
                }
            }

            if (logger.isInfoEnabled()) {
                logger.info(module + ": Socket queued at serversocket");
            }

        } catch (Exception e) {

            sendReply(target, requestID, e.getMessage());

            if (logger.isInfoEnabled()) {
                logger.info(module + ": Connection to " + target + " failed!", e);
            }
        }
    }

    private void handleCannotConnectMessage(byte [][] message) {

        int requestID;
        String reply = null;

        try {
            requestID = toInt(message[0]);
            reply = toString(message[1]);
        } catch (Exception e) {
            logger.warn(module + ": failed to parse reply message!", e);
            return;
        }

        if (logger.isInfoEnabled()) {
            logger.info(module + ": connection reply (" + requestID + "): "
                    + reply);
        }

        storeReply(requestID, reply);
    }

    private void handleConnectMessageFailed(byte [][] message) {

        int requestID;

        try {
            requestID = toInt(message[6]);
        } catch (Exception e) {
            logger.warn(module + ": failed to parse connect message!", e);
            return;
        }

        if (logger.isInfoEnabled()) {
            logger.info(module + ": connection request failed (" + requestID + ")");
        }

        storeReply(requestID, "Target not reachable");
    }

    private void handleConnectMessage(byte [][] message) {

        int timeout = 0;
        int localport = 0;
        VirtualSocketAddress target = null;
        int requestID;

        try {
            localport                   = toInt(message[0]);
            DirectSocketAddress machine = toSocketAddressSet(message[1]);
            int port                    = toInt(message[2]);
            DirectSocketAddress hub     = toSocketAddressSet(message[3]);
            String cluster              = toString(message[4]);
            timeout                     = toInt(message[5]);
            requestID                   = toInt(message[6]);

            target = new VirtualSocketAddress(machine, port, hub, cluster);
        } catch (Exception e) {
            logger.warn(module + ": failed to parse connect message!", e);
            return;
        }

        if (logger.isInfoEnabled()) {
            logger.info(module + ": connection request (" + requestID + "): "
                    + " local port: " + localport + ", target " + target
                    + ", timeout " + timeout);
        }

        VirtualServerSocket ss = parent.getServerSocket(localport);

        if (ss == null) {

            sendReply(target, requestID, "Port " + localport + " not found");

            if (logger.isInfoEnabled()) {
                logger.info(module + ": port " + localport + " not found!");
            }
            return;
        }

        if (timeout <= 0) {
            timeout = defaultConnectTimeout;
        }

        if (USE_THREAD) {
            new Connector(ss, target, timeout, requestID).start();
        } else {
            setupConnection(ss, target, timeout, requestID);
        }
    }

    public void gotMessage(DirectSocketAddress src, DirectSocketAddress srcProxy,
            int opcode, boolean returnToSender, byte [][] message) {

        if (logger.isInfoEnabled()) {
            logger.info(module + ": handling connection request from " + src + "@" +
                    srcProxy + " message = \"" + Arrays.toString(message) + "\"");
        }

        switch (opcode) {
        case PLEASE_CONNECT:
            if (returnToSender) {
                handleConnectMessageFailed(message);
            } else {
                handleConnectMessage(message);
            }
            break;

        case CANNOT_CONNECT:
            if (returnToSender) {
                // ignore
            } else {
                handleCannotConnectMessage(message);
            }
            break;

        default:
            logger.warn(module + " got unexpected message from " + src + "@"
                        + srcProxy + " message = \""
                        + Arrays.deepToString(message) + "\"");
        }
    }

    public boolean matchAdditionalRuntimeRequirements(Map<String, ?> requirements) {
        // Nothing to check here ?
        return true;
    }

    public int getDefaultTimeout() {
        return defaultConnectTimeout;
    }
}
