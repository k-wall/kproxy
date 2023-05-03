/*
 * Copyright Kroxylicious Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */

package io.kroxylicious.proxy.internal.net;

public class EndpointBindingException extends EndpointException {
    public EndpointBindingException(String message) {
        super(message);
    }

    public EndpointBindingException(String message, Throwable cause) {
        super(message, cause);
    }
}
