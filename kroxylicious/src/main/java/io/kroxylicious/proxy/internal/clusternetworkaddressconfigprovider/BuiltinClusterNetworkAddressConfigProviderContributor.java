/*
 * Copyright Kroxylicious Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.kroxylicious.proxy.internal.clusternetworkaddressconfigprovider;

import io.kroxylicious.proxy.clusternetworkaddressconfigprovider.ClusterNetworkAddressConfigProviderContributor;
import io.kroxylicious.proxy.internal.clusternetworkaddressconfigprovider.PortPerBrokerClusterNetworkAddressConfigProvider.PortPerBrokerClusterNetworkAddressConfigProviderConfig;
import io.kroxylicious.proxy.internal.clusternetworkaddressconfigprovider.SniRoutingClusterNetworkAddressConfigProvider.SniRoutingClusterNetworkAddressConfigProviderConfig;
import io.kroxylicious.proxy.service.BaseContributor;
import io.kroxylicious.proxy.service.ClusterNetworkAddressConfigProvider;
import io.kroxylicious.proxy.service.Context;

public class BuiltinClusterNetworkAddressConfigProviderContributor extends BaseContributor<ClusterNetworkAddressConfigProvider, Context>
        implements ClusterNetworkAddressConfigProviderContributor {

    public static final BaseContributorBuilder<ClusterNetworkAddressConfigProvider, Context> FILTERS = BaseContributor
            .<ClusterNetworkAddressConfigProvider, Context> builder()
            .add("PortPerBroker", PortPerBrokerClusterNetworkAddressConfigProviderConfig.class, PortPerBrokerClusterNetworkAddressConfigProvider::new)
            .add("SniRouting", SniRoutingClusterNetworkAddressConfigProviderConfig.class, SniRoutingClusterNetworkAddressConfigProvider::new);

    public BuiltinClusterNetworkAddressConfigProviderContributor() {
        super(FILTERS);
    }
}