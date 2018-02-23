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
package ibis.smartsockets.hub.connections;

import ibis.smartsockets.hub.Statistics;

import java.io.PrintStream;

public class MessageForwardingConnectionStatistics extends Statistics {

    long connectionsTotal;
    long connectionsFailed;

    long connectionsReplies;
    long connectionsACKs;
    long connectionsNACKs;
    long connectionsRepliesLost;
    long connectionsRepliesError;

    long closeTotal;
    long closeError;
    long closeLost;

    long messages;
    long messagesError;
    long messagesLost;
    long messagesBytes;

    long messageACK;
    long messageACK_Error;
    long messageACKLost;

    long infoMessages;
    long infoMessagesBytes;
    long infoMessagesForwarded;
    long infoMessagesDropped;
    long infoMessagesReturned;
    long infoMessagesDelivered;
    long infoMessagesFailed;

    public MessageForwardingConnectionStatistics(String name) {
        super(name);
    }

    public void add(Statistics tmp) {

        MessageForwardingConnectionStatistics other =
            (MessageForwardingConnectionStatistics) tmp;

        connectionsTotal += other.connectionsTotal;
        connectionsFailed += other.connectionsFailed;

        connectionsReplies += other.connectionsReplies;
        connectionsACKs += other.connectionsACKs;
        connectionsNACKs += other.connectionsNACKs;
        connectionsRepliesLost += other.connectionsRepliesLost;
        connectionsRepliesError += other.connectionsRepliesError;

        closeTotal += other.closeTotal;
        closeError += other.closeTotal;
        closeLost += other.closeLost;

        messages += other.messages;
        messagesError += other.messagesError;
        messagesLost += other.messagesLost;
        messagesBytes += other.messagesBytes;

        messageACK += other.messageACK;
        messageACK_Error += other.messageACK_Error;
        messageACKLost += other.messageACKLost;

        infoMessages += other.infoMessages;
        infoMessagesBytes += other.infoMessagesBytes;
        infoMessagesForwarded += other.infoMessagesForwarded;
        infoMessagesDropped += other.infoMessagesDropped;
        infoMessagesReturned += other.infoMessagesReturned;
        infoMessagesDelivered += other.infoMessagesDelivered;
        infoMessagesFailed += other.infoMessagesFailed;
    }

    public void print(PrintStream out, String prefix) {
        out.println(prefix + "VConnections: " + connectionsTotal);
        out.println(prefix + "   - failed : " + connectionsFailed);
        out.println(prefix + "   - lost   : " + connectionsRepliesLost);
        out.println(prefix + "   - error  : " + connectionsRepliesError);
        out.println(prefix + "VReplies    : " + connectionsReplies);
        out.println(prefix + " - ACK      : " + connectionsACKs);
        out.println(prefix + " - rejected : " + connectionsNACKs);
        out.println(prefix + " - lost     : " + connectionsRepliesLost);
        out.println(prefix + " - error    : " + connectionsRepliesError);
        out.println(prefix + "VMessages   : " + messages);
        out.println(prefix + " - bytes    : " + messagesBytes);
        out.println(prefix + " - lost     : " + messagesLost);
        out.println(prefix + " - error    : " + messagesError);
        out.println(prefix + "VMess. ACKS : " + messageACK);
        out.println(prefix + "    - lost  : " + messageACKLost);
        out.println(prefix + "    - error : " + messageACK_Error);
        out.println(prefix + "IMessages   : " + infoMessages);
        out.println(prefix + " - bytes    : " + infoMessagesBytes);
        out.println(prefix + " - delivered: " + infoMessagesDelivered);
        out.println(prefix + " - forwarded: " + infoMessagesForwarded);
        out.println(prefix + " - failed fw: " + infoMessagesFailed);
        out.println(prefix + " - returned : " + infoMessagesReturned);
    }
}
