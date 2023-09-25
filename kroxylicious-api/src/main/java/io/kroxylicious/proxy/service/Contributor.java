/*
 * Copyright Kroxylicious Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.kroxylicious.proxy.service;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Support loading an Instance of a service, optionally providing it with configuration obtained
 * from the Kroxylicious configuration file.
 *
 * @param <S> the service type
 * @param <C> the type of config provided to the service, or {@link Void} for config-less service implementations.
 * @param <X> the context type
 */
public interface Contributor<S, C, X extends Context<C>> {

    /**
     * Identifies the type name this contributor offers.
     * @return typeName
     */
    @NonNull
    String getTypeName();

    /**
     * The type of config expected by the service.
     * <br/>
     * The type must have a constructor annotated with the JsonCreator annotation.
     * If the service does not required configuration, return {@link Void} instead.
     *
     * @return type of config expected by the service.
     */
    @NonNull
    Class<C> getConfigType();

    @NonNull
    default boolean requiresConfiguration() {
        return false;
    }

    /**
     * Creates an instance of the service.
     *
     * @param context   context containing service configuration which may be null if the service instance does not accept configuration.
     * @return the service instance.
     */
    @NonNull
    S getInstance(X context);

}
