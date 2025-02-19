/*
 * Copyright Kroxylicious Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.kroxylicious.proxy.config;

import java.util.List;
import java.util.Map;
import java.util.Optional;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.kroxylicious.proxy.config.tls.Tls;

import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * A virtual cluster.
 *
 * @param targetCluster the cluster being proxied
 * @param clusterNetworkAddressConfigProvider virtual cluster network config - deprecated - use a named listener
 * @param tls deprecated - tls settings for the virtual cluster - deprecated - use a named listener
 * @param listeners listeners
 * @param logNetwork if true, network will be logged
 * @param logFrames if true, kafka rpcs will be logged
 * @param filters filers.
 */
public record VirtualCluster(TargetCluster targetCluster,
                             @Deprecated(forRemoval = true, since = "0.11.0") ClusterNetworkAddressConfigProviderDefinition clusterNetworkAddressConfigProvider,
                             @Deprecated(forRemoval = true, since = "0.11.0") @JsonProperty() Optional<Tls> tls,

                             @JsonProperty(required = false) Map<String, VirtualClusterListener> listeners,
                             boolean logNetwork,
                             boolean logFrames,
                             @Nullable List<String> filters) {

    private static final Logger LOGGER = LoggerFactory.getLogger(VirtualCluster.class);

    @SuppressWarnings("removal")
    public VirtualCluster {
        if (clusterNetworkAddressConfigProvider != null || tls.isPresent()) {
            if (listeners == null || listeners.isEmpty()) {
                LOGGER.warn(
                        "The virtualCluster properties 'clusterNetworkAddressConfigProvider' and 'tls' are deprecated, specify virtual cluster listeners using the listeners map.");
                listeners = Map.of("default", new VirtualClusterListener(clusterNetworkAddressConfigProvider, tls));
            }
            else {
                throw new IllegalConfigurationException("When using listeners, the virtualCluster properties 'clusterNetworkAddressConfigProvider' and 'tls' must be omitted");
            }
        }
    }

    public ClusterNetworkAddressConfigProviderDefinition clusterNetworkAddressConfigProvider() {
        throw new UnsupportedOperationException();
    }

    public Optional<Tls> tls() {
        throw new UnsupportedOperationException();
    }

    /**
     * A virtual cluster listener.
     *
     * @param clusterNetworkAddressConfigProvider network config
     * @param tls tls settings
     */
    public record VirtualClusterListener(@JsonProperty(required = true) ClusterNetworkAddressConfigProviderDefinition clusterNetworkAddressConfigProvider,
                                         Optional<Tls> tls) {

    }
}
