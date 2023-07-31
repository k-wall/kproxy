/*
 * Copyright Kroxylicious Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */

package io.kroxylicious.proxy.filter;

import org.apache.kafka.common.message.RequestHeaderData;
import org.apache.kafka.common.protocol.ApiMessage;

public interface RequestFilterResult<M extends ApiMessage> extends FilterResult<M, RequestHeaderData> {

    ResponseFilterResult<ApiMessage> shortCircuitResponse();
}
