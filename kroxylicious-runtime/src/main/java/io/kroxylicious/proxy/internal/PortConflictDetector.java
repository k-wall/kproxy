/*
 * Copyright Kroxylicious Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */

package io.kroxylicious.proxy.internal;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

import io.kroxylicious.proxy.internal.net.EndpointListener;
import io.kroxylicious.proxy.model.VirtualClusterModel;
import io.kroxylicious.proxy.service.HostPort;

/**
 * Detects potential for port conflicts arising between virtual cluster configurations.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class PortConflictDetector {

    private static final String ANY_STRING = "<any>";

    private enum BindingScope {
        SHARED,
        EXCLUSIVE
    }

    private record BindHost(Optional<String> host) {
        boolean overlaps(BindHost other) {
            return isAnyHost() || other.isAnyHost() || host.equals(other.host);
        }

        boolean isAnyHost() {
            return host.isEmpty();
        }

        @Override
        public String toString() {
            return host.orElse(ANY_STRING);
        }
    }

    private record CandidateBinding(EndpointListener listener, int port, BindHost bindHost, BindingScope scope) {
        void validateNonConflicting(CandidateBinding other) {
            if (this.port == other.port && bindHost.overlaps(other.bindHost)) {
                if (scope == BindingScope.EXCLUSIVE || other.scope == BindingScope.EXCLUSIVE) {
                    throw new IllegalStateException(this + " conflicts with " + other + ": exclusive port collision");
                }
                if (!bindHost.equals(other.bindHost)) {
                    throw new IllegalStateException(this + " conflicts with " + other + ": shared port cannot bind to different hosts");
                }
                if (listener.isUseTls() != other.listener.isUseTls()) {
                    throw new IllegalStateException(this + " conflicts with " + other + ": shared port cannot be both TLS and non-TLS");
                }
            }
        }

        void validateNonConflicting(HostPort otherExclusivePort) {
            boolean isAnyHost = otherExclusivePort.host().equals("0.0.0.0");
            BindHost otherBindHost = new BindHost(isAnyHost ? Optional.empty() : Optional.of(otherExclusivePort.host()));
            if (this.port == otherExclusivePort.port() && bindHost.overlaps(otherBindHost)) {
                throw new IllegalStateException(this + " conflicts with another (non-cluster) exclusive port " + otherExclusivePort);
            }
        }

        @Override
        public String toString() {
            return scope.name().toLowerCase(Locale.ENGLISH) + " " + (listener.isUseTls() ? "TLS" : "TCP") + " bind of " + bindHost + ":" + port +
                    " for listener '" + listener.name() + "' of virtual cluster '" + listener.virtualCluster().getClusterName() + "'";
        }
    }

    /**
     * Validates the configuration throwing an exception if a conflict is detected.
     *
     * @param virtualClusterModelMap map of virtual clusters.
     * @param otherExclusivePort an optional exclusive port that should conflict with virtual cluster ports
     */
    public void validate(Collection<VirtualClusterModel> virtualClusterModelMap, Optional<HostPort> otherExclusivePort) {
        List<CandidateBinding> candidateBindings = virtualClusterModelMap.stream().sorted(
                Comparator.comparing(VirtualClusterModel::getClusterName))
                .flatMap(m -> m.listeners().values().stream().sorted(Comparator.comparing(EndpointListener::name))).flatMap(listener -> {
                    Stream<CandidateBinding> exclusiveBindings = listener.getExclusivePorts().stream().sorted()
                            .map(exclusivePort -> new CandidateBinding(listener, exclusivePort, new BindHost(listener.getBindAddress()), BindingScope.EXCLUSIVE));
                    Stream<CandidateBinding> sharedBindings = listener.getSharedPorts().stream().sorted()
                            .map(exclusivePort -> new CandidateBinding(listener, exclusivePort, new BindHost(listener.getBindAddress()), BindingScope.SHARED));
                    return Stream.concat(exclusiveBindings, sharedBindings);
                }).toList();
        for (int i = 0; i < candidateBindings.size(); i++) {
            CandidateBinding candidateBinding = candidateBindings.get(i);
            otherExclusivePort.ifPresent(candidateBinding::validateNonConflicting);
            for (int j = i + 1; j < candidateBindings.size(); j++) {
                candidateBinding.validateNonConflicting(candidateBindings.get(j));
            }
        }
    }

}
