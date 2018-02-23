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

import java.io.Serializable;

/**
 * This class encapsulates a Throwable and a description (String) of its origin.
 *
 * @author Jason Maassen
 * @version 1.0 Dec 19, 2005
 * @since 1.0
 *
 */
public class NestedIOExceptionData implements Serializable {

    /**
     * Generated
     */
    private static final long serialVersionUID = 6049338700587184766L;

    public final String description;
    public final Throwable cause;

    public NestedIOExceptionData(String description, Throwable cause) {
        this.description = description;
        this.cause = cause;
    }
}
