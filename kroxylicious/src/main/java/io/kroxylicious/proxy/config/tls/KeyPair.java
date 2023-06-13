/*
 * Copyright Kroxylicious Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */

package io.kroxylicious.proxy.config.tls;

/**
 * Specifies key pair (private key/certificate) configuration..
 *
 * @param privateKeyFile  location of a file containing the server's private key. cannot be used if storeFile is specified
 * @param certificateFile location of a file containing the server certificate and intermediates.  privateKeyFile is required if this option is used.
 * @param keyPassword     password used to protect the key within the storeFile or privateKeyFile
 */
public record KeyPair(String privateKeyFile,
                      String certificateFile,
                      PasswordProvider keyPassword
) {

}
