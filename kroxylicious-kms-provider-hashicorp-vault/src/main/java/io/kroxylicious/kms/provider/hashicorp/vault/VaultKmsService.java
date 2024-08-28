/*
 * Copyright Kroxylicious Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */

package io.kroxylicious.kms.provider.hashicorp.vault;

import java.time.Duration;

import io.kroxylicious.kms.provider.hashicorp.vault.config.Config;
import io.kroxylicious.kms.service.KmsService;
import io.kroxylicious.proxy.plugin.Plugin;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * An implementation of the {@link KmsService} interface backed by a remote instance of HashiCorp Vault.
 */
@Plugin(configType = Config.class)
public class VaultKmsService implements KmsService<Config, Config, String, VaultEdek> {

    @NonNull
    @Override
    public VaultKms buildKms(Config initializationData) {
        return new VaultKms(initializationData.vaultTransitEngineUrl(), initializationData.vaultToken().getProvidedPassword(), Duration.ofSeconds(20),
                initializationData.sslContext());
    }

}
