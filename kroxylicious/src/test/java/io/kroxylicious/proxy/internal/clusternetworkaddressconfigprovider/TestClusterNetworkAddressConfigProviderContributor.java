/*
 * Copyright Kroxylicious Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */

package io.kroxylicious.proxy.internal.clusternetworkaddressconfigprovider;

import org.jetbrains.annotations.NotNull;

import io.kroxylicious.proxy.clusternetworkaddressconfigprovider.ClusterNetworkAddressConfigProviderContributor;
import io.kroxylicious.proxy.service.ClusterNetworkAddressConfigProvider;
import io.kroxylicious.proxy.service.ConfigurationDefinition;
import io.kroxylicious.proxy.service.Context;

public class TestClusterNetworkAddressConfigProviderContributor implements ClusterNetworkAddressConfigProviderContributor {

    public static final String SHORT_NAME = "test";

    @NotNull
    @Override
    public String getTypeName() {
        return SHORT_NAME;
    }

    @Override
    public ConfigurationDefinition getConfigDefinition() {
        return new ConfigurationDefinition(Config.class, true);
    }

    @Override
    public ClusterNetworkAddressConfigProvider getInstance(Context context) {
        return new TestClusterNetworkAddressConfigProvider(SHORT_NAME, context.getConfig(), context);
    }
}