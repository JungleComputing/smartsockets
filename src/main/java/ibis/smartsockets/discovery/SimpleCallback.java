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
package ibis.smartsockets.discovery;

import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleCallback implements Callback {

    private static final Logger logger =
        LoggerFactory.getLogger("ibis.smartsockets.discovery");

    private final LinkedList<String> messages = new LinkedList<String>();
    private final boolean quitAfterMessage;

    public SimpleCallback(boolean quitAfterMessage) {
        this.quitAfterMessage = quitAfterMessage;
    }

    public synchronized String get(long timeout) {

        long end = System.currentTimeMillis() + timeout;
        long left = timeout;

        while (messages.size() == 0) {
            try {
                wait(left);
            } catch (InterruptedException e) {
                // ignore
            }

            left = end - System.currentTimeMillis();

            if (timeout > 0 && left <= 0) {
                return null;
            }
        }

        return messages.removeFirst();
    }

    public synchronized boolean gotMessage(String message) {

        if (message != null) {
            if (logger.isInfoEnabled()) {
                logger.info("Received: \"" + message + "\"");
            }
            messages.addLast(message);
            notifyAll();
            return !quitAfterMessage;
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("Discarding message: <null>");
            }
            return true;
        }
    }
}
