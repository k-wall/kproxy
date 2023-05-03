/*
 * Copyright Kroxylicious Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.kroxylicious.proxy.service;

import java.util.Optional;

/**
 * Provides the addresses of the network endpoints required by a virtual cluster.
 */
public interface ClusterEndpointConfigProvider {

    /**
     * Address of the cluster's bootstrap address.
     *
     * @return cluster's bootstrap address.
     */
    HostPort getClusterBootstrapAddress();

    /**
     * Address of broker with the given node id, includes the port. Note that
     * {@code nodeId} are generally expected to be consecutively numbered and starting from zero. However, gaps in the sequence can potentially emerge as
     * the target cluster topology evolves.
     *
     * @param nodeId node identifier
     * @return broker address
     * @throws IllegalArgumentException if this provider cannot produce a broker address for the given nodeId.
     */
    HostPort getBrokerAddress(int nodeId) throws IllegalArgumentException;

    /**
     * Provides the number of broker endpoints to pre-bind.
     * Kroxylicious will pre-bind all the broker end-points between
     * 0..{@code numberOfWarmStartBrokerEndpoints}-1 on startup, rather
     * than waiting for the first bootstrap connection to allow their
     * discovery.
     *
     * @return number of endpoints to pre-bind on startup.
     */
    default int getNumberOfBrokerEndpointsToPrebind() {
        return 1;
    }

    /**
     * Gets the bind address used when binding socket.  Used to restrict
     * listening to particular network interfaces.
     * @return bind address such as "127.0.0.1" or {@code }Optional.empty()} if all address should be bound.
     */
    default Optional<String> getBindAddress() {
        return Optional.empty();
    }

    /**
     * Indicates if this provider requires exclusive use of ports that it defines.  If true,
     * no other provider will be able to bind the same port.
     *
     * @return true if this endpoint provider requires exclusive use of ports.
     */
    default boolean requiresPortExclusivity() {
        return true;
    }

    /**
     * Indicates if this provider requires the use of TLS.
     *
     * @return true if this provider requires the use of TLS.
     */
    default boolean requiresTls() {
        return false;
    }

    /**
     * Encapsulates the result of a endpoint match.
     *
     * @param matched true if matched, false otherwise.
     * @param nodeId if a broker endpoint match, carries the {@code nodeId}.
     */
    record EndpointMatchResult(boolean matched, Integer nodeId) {
    }
}
