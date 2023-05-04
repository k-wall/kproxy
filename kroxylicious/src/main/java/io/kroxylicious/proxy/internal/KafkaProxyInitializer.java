/*
 * Copyright Kroxylicious Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.kroxylicious.proxy.internal;

import java.util.Map;

import org.apache.kafka.common.security.auth.AuthenticateCallbackHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SniHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.util.concurrent.Future;

import io.kroxylicious.proxy.config.Configuration;
import io.kroxylicious.proxy.config.VirtualCluster;
import io.kroxylicious.proxy.internal.codec.KafkaRequestDecoder;
import io.kroxylicious.proxy.internal.codec.KafkaResponseEncoder;
import io.kroxylicious.proxy.internal.filter.UpstreamBrokerAddressCachingNetFilter;
import io.kroxylicious.proxy.internal.net.EndpointResolver;

public class KafkaProxyInitializer extends ChannelInitializer<SocketChannel> {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProxyInitializer.class);

    private final boolean haproxyProtocol;
    private final Map<KafkaAuthnHandler.SaslMechanism, AuthenticateCallbackHandler> authnHandlers;
    private final boolean tls;
    private final EndpointResolver endpointResolver;
    private final Configuration config;

    public KafkaProxyInitializer(Configuration config, boolean tls, EndpointResolver endpointResolver, boolean haproxyProtocol,
                                 Map<KafkaAuthnHandler.SaslMechanism, AuthenticateCallbackHandler> authnMechanismHandlers) {
        this.haproxyProtocol = haproxyProtocol;
        this.authnHandlers = authnMechanismHandlers != null ? authnMechanismHandlers : Map.of();
        this.tls = tls;
        this.endpointResolver = endpointResolver;
        this.config = config;
    }

    @Override
    public void initChannel(SocketChannel ch) {

        LOGGER.trace("Connection from {} to my address {}", ch.remoteAddress(), ch.localAddress());

        ChannelPipeline pipeline = ch.pipeline();

        int targetPort = ch.localAddress().getPort();
        var bindingAddress = ch.parent().localAddress().getAddress().isAnyLocalAddress() ? null : ch.localAddress().getAddress().getHostAddress();
        if (tls) {
            LOGGER.debug("Adding SSL/SNI handler");
            pipeline.addLast(new SniHandler((sniHostname, promise) -> {
                try {
                    var stage = endpointResolver.resolve(bindingAddress, targetPort, sniHostname, tls);
                    stage.handle((binding, t) -> {
                        if (t != null) {
                            promise.setFailure(t);
                            return null;
                        }
                        var virtualCluster = binding.virtualCluster();
                        var sslContext = virtualCluster.buildSslContext();
                        if (sslContext.isEmpty()) {
                            promise.setFailure(new IllegalStateException("Virtual cluster %s does not provide SSL context".formatted(virtualCluster)));
                            return null;
                        }
                        else {
                            KafkaProxyInitializer.this.addHandlers(ch, virtualCluster);
                            promise.setSuccess(sslContext.get());
                            return null;
                        }
                    });
                    // return value of handle deliberately ignored.
                    return promise;
                }
                catch (Throwable cause) {
                    return promise.setFailure(cause);
                }
            }) {

                @Override
                protected void onLookupComplete(ChannelHandlerContext ctx, Future<SslContext> future) throws Exception {
                    super.onLookupComplete(ctx, future);
                    ctx.fireChannelActive();
                }
            });
        }
        else {
            pipeline.addLast(new ChannelInboundHandlerAdapter() {
                @Override
                public void channelActive(ChannelHandlerContext ctx) {
                    var stage = endpointResolver.resolve(bindingAddress, targetPort, null, tls);
                    stage.handle((binding, t) -> {
                        if (t != null) {
                            ctx.fireExceptionCaught(t);
                            return null;
                        }
                        var virtualCluster = binding.virtualCluster();
                        KafkaProxyInitializer.this.addHandlers(ch, virtualCluster);
                        ctx.fireChannelActive();
                        pipeline.remove(this);
                        return null;
                    });
                    // return value of handle deliberately ignored.
                }
            });
        }
    }

    private void addHandlers(SocketChannel ch, VirtualCluster virtualCluster) {
        ChannelPipeline pipeline = ch.pipeline();
        if (virtualCluster.isLogNetwork()) {
            pipeline.addLast("networkLogger", new LoggingHandler("io.kroxylicious.proxy.internal.DownstreamNetworkLogger", LogLevel.INFO));
        }

        // Add handler here
        // TODO https://github.com/kroxylicious/kroxylicious/issues/287 this is in the wrong place, proxy protocol comes over the wire first (so before SSL handler).
        if (haproxyProtocol) {
            LOGGER.debug("Adding haproxy handler");
            pipeline.addLast("HAProxyMessageDecoder", new HAProxyMessageDecoder());
        }

        var dp = new SaslDecodePredicate(!authnHandlers.isEmpty());
        // The decoder, this only cares about the filters
        // because it needs to know whether to decode requests
        KafkaRequestDecoder decoder = new KafkaRequestDecoder(dp);
        pipeline.addLast("requestDecoder", decoder);

        pipeline.addLast("responseEncoder", new KafkaResponseEncoder());
        if (virtualCluster.isLogFrames()) {
            pipeline.addLast("frameLogger", new LoggingHandler("io.kroxylicious.proxy.internal.DownstreamFrameLogger", LogLevel.INFO));
        }

        if (!authnHandlers.isEmpty()) {
            LOGGER.debug("Adding authn handler for handlers {}", authnHandlers);
            pipeline.addLast(new KafkaAuthnHandler(ch, authnHandlers));
        }

        var netFilter = new UpstreamBrokerAddressCachingNetFilter(config, virtualCluster);

        pipeline.addLast("netHandler", new KafkaProxyFrontendHandler(netFilter, dp, virtualCluster.isLogNetwork(), virtualCluster.isLogFrames()));
        LOGGER.debug("{}: Initial pipeline: {}", ch, pipeline);
    }

}
