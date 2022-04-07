/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.strimzi.kproxy.interceptor;

import java.util.List;
import java.util.stream.Collectors;

import io.netty.channel.ChannelInboundHandler;
import io.strimzi.kproxy.codec.DecodePredicate;
import org.apache.kafka.common.protocol.ApiKeys;

/**
 * Combines a number of interceptors
 */
public class InterceptorProvider implements DecodePredicate {
    private final List<Interceptor> interceptors;

    public InterceptorProvider(List<Interceptor> interceptors) {
        this.interceptors = interceptors;
    }

    public List<ChannelInboundHandler> frontendHandlers() {
        return interceptors.stream().map(Interceptor::frontendHandler).collect(Collectors.toList());
    }

    public List<ChannelInboundHandler> backendHandlers() {
        return interceptors.stream().map(Interceptor::backendHandler).collect(Collectors.toList());
    }

    @Override
    public boolean shouldDecodeRequest(ApiKeys apiKey, int apiVersion) {
        for (var interceptor : interceptors) {
            if (interceptor.shouldDecodeRequest(apiKey, apiVersion)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldDecodeResponse(ApiKeys apiKey, int apiVersion) {
        for (var interceptor : interceptors) {
            if (interceptor.shouldDecodeResponse(apiKey, apiVersion)) {
                return true;
            }
        }
        return false;
    }
}
