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
import java.util.LinkedList;

/**
 * This class encapsulates a set of IOExceptions.
 *
 * @author Jason Maassen
 * @version 1.0 Dec 19, 2005
 * @since 1.0
 *
 */
public class NestedIOException extends IOException {

    private static final long serialVersionUID = 7906462081071080010L;

    private final String [] names;
    private final Throwable [] causes;

    private final LinkedList<NestedIOExceptionData> exceptions;

    public NestedIOException(String message,
            LinkedList<NestedIOExceptionData> exceptions) {

        super(message);
        this.names = null;
        this.causes = null;
        this.exceptions = exceptions;
    }

    public NestedIOException(String message, String [] names,
            Throwable [] causes) {

        super(message);
        this.names = names.clone();
        this.causes = causes.clone();
        this.exceptions = null;
    }

    public String toString() {
        return toString("       ");
    }

    public String toString(String pre) {
        String name = getClass().getName();
        String message = getLocalizedMessage();

        StringBuilder builder = new StringBuilder();

        builder.append(name);

        if (message != null) {
            builder.append(": ");
            builder.append(message);
        }

        if (exceptions != null) {

            for (NestedIOExceptionData d : exceptions) {
                builder.append("\n");
                builder.append(pre);
                builder.append(d.description);
                builder.append(": ");
                builder.append(d.cause.toString());
            }

        } else if (causes != null) {
            for (int i=0;i<causes.length;i++) {
                builder.append("\n");
                builder.append(pre);
                builder.append(names[i]);
                builder.append(": ");

                if (causes[i] != null) {
                    builder.append(causes[i].toString());
                } else {
                    builder.append("<unknown cause>");
                }
            }
        } else {
            builder.append(" (no information)");
        }

        return builder.toString();
    }
}
