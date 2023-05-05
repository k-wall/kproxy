/*
 * Copyright Kroxylicious Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */

package io.kroxylicious.proxy.internal.net;

import java.util.Objects;

import io.kroxylicious.proxy.config.VirtualCluster;

public final class VirtualClusterBrokerBinding extends VirtualClusterBinding {
    private final int nodeId;

    public VirtualClusterBrokerBinding(VirtualCluster virtualCluster, int nodeId) {
        super(virtualCluster);
        this.nodeId = nodeId;
    }

    public int nodeId() {
        return nodeId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (io.kroxylicious.proxy.internal.net.VirtualClusterBrokerBinding) obj;
        return super.equals(obj) && Objects.equals(this.nodeId, that.nodeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), nodeId);
    }

    @Override
    public String toString() {
        return "VirtualClusterBrokerBinding[" +
                "virtualCluster=" + this.virtualCluster() + ", " +
                "nodeId=" + nodeId + ']';
    }

}
