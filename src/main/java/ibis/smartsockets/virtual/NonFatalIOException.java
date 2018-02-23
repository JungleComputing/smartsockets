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

public class NonFatalIOException extends Exception {

    private static final long serialVersionUID = 1220215084768722435L;

    public NonFatalIOException(String message) {
        super(message);
    }

    public NonFatalIOException(Throwable cause) {
        super(null, cause);
    }

    public NonFatalIOException(String message, Throwable cause) {
        super(message, cause);
    }

    public String toString() {

        if (getCause() == null) {
            return getClass().getName() + ": " + getLocalizedMessage();
        }

        if (getLocalizedMessage() == null) {
            return getCause().toString();
        }

        return getClass().getName() + ": " + getLocalizedMessage() + " -> "
            + getCause().toString();
    }
}
