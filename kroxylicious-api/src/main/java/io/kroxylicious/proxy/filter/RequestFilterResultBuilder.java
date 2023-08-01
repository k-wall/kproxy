/*
 * Copyright Kroxylicious Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */

package io.kroxylicious.proxy.filter;

import org.apache.kafka.common.message.RequestHeaderData;
import org.apache.kafka.common.message.ResponseHeaderData;
import org.apache.kafka.common.protocol.ApiMessage;

import io.kroxylicious.proxy.filter.filterresultbuilder.CloseOrTerminalStage;

public interface RequestFilterResultBuilder extends FilterResultBuilder<RequestHeaderData, RequestFilterResult> {

    CloseOrTerminalStage<RequestFilterResult> shortCircuitResponse(ResponseHeaderData header, ApiMessage message);

    CloseOrTerminalStage<RequestFilterResult> shortCircuitResponse(ApiMessage message);

}