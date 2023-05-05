/*
 * Copyright Kroxylicious Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */

package io.kroxylicious.proxy.internal.net;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

public class NetworkBindRequest extends NetworkBindingOperation<Channel> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkBindRequest.class);
    private final String bindingAddress;
    private final int port;
    private final CompletableFuture<Channel> future;

    public NetworkBindRequest(String bindingAddress, int port, boolean tls, CompletableFuture<Channel> future) {
        super(tls);
        this.bindingAddress = bindingAddress;
        this.port = port;
        this.future = future;
    }

    public String getBindingAddress() {
        return bindingAddress;
    }

    @Override
    public int port() {
        return port;
    }

    @Override
    public CompletableFuture<Channel> getFuture() {
        return future;
    }

    @Override
    public void performBindingOperation(ServerBootstrap serverBootstrap) {
        try {
            int port = port();
            ChannelFuture bind;
            if (bindingAddress != null) {
                LOGGER.info("Binding {}:{}", bindingAddress, port);
                bind = serverBootstrap.bind(bindingAddress, port);
            }
            else {
                LOGGER.info("Binding <any>:{}", port);
                bind = serverBootstrap.bind(port);
            }
            bind.addListener((ChannelFutureListener) channelFuture -> {
                if (channelFuture.cause() != null) {
                    future.completeExceptionally(channelFuture.cause());
                }
                else {
                    future.complete(channelFuture.channel());
                }
            });
        }
        catch (Throwable t) {
            future.completeExceptionally(t);
        }
    }

}
