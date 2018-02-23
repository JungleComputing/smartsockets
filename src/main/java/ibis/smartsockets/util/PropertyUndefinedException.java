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
package ibis.smartsockets.util;

/**
 * Like java.lang.Exception, but with a cause.
 */
public class PropertyUndefinedException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs an <code>PropertyUndefinedException</code> with <code>null</code> as
     * its error detail message.
     */
    public PropertyUndefinedException() {
        super();
    }

    /**
     * Constructs an <code>PropertyUndefinedException</code> with the specified detail
     * message.
     *
     * @param s
     *            the detail message
     */
    public PropertyUndefinedException(String s) {
        super(s);
    }

    /**
     * Constructs an <code>PropertyUndefinedException</code> with the specified detail
     * message and cause.
     *
     * @param s
     *            the detail message
     * @param cause
     *            the cause
     */
    public PropertyUndefinedException(String s, Throwable cause) {
        super(s, cause);
    }

    /**
     * Constructs an <code>PropertyUndefinedException</code> with the specified cause.
     *
     * @param cause
     *            the cause
     */
    public PropertyUndefinedException(Throwable cause) {
        super(cause);
    }

    public String toString() {
        String message = super.getMessage();
        Throwable cause = getCause();
        if (message == null) {
            message = "";
        }
        if (cause != null) {
            message += ": " + cause.getMessage();
        }

        return message;
    }
}
