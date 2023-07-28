/*
 * Copyright Kroxylicious Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */

package io.kroxylicious.proxy.filter;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.common.message.ResponseHeaderData;
import org.apache.kafka.common.protocol.ApiKeys;
import org.apache.kafka.common.protocol.ApiMessage;

public class ResponseForwardDelayingFilter implements ResponseFilter {

    @Override
    public CompletionStage<ResponseFilterResult> onResponse(ApiKeys apiKey, ResponseHeaderData header, ApiMessage body, KrpcFilterContext filterContext) {
        CompletableFuture<ResponseFilterResult> future = new CompletableFuture<>();
        FilterResultBuilder<ResponseFilterResult, ResponseHeaderData> builder = filterContext.responseFilterResultBuilder();
        try (var executor = Executors.newScheduledThreadPool(1)) {
            var delay = (long) (Math.random() * 200);
            executor.schedule(() -> {
                builder.withMessage(body);
                builder.withHeader(header);
                future.complete(builder.build());
            }, delay, TimeUnit.MILLISECONDS);
        }
        return future;
    }

}