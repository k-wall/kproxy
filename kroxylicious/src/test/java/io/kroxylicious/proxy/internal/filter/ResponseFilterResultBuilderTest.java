/*
 * Copyright Kroxylicious Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */

package io.kroxylicious.proxy.internal.filter;

import org.apache.kafka.common.message.FetchRequestData;
import org.apache.kafka.common.message.FetchResponseData;
import org.apache.kafka.common.message.ResponseHeaderData;
import org.junit.jupiter.api.Test;

import io.kroxylicious.proxy.filter.ResponseFilterResultBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResponseFilterResultBuilderTest {

    private final ResponseFilterResultBuilder builder = new ResponseFilterResultBuilderImpl();

    @Test
    void forwardResponse() {
        var res = new FetchResponseData();
        var head = new ResponseHeaderData();
        var result = builder.forward(head, res).build();
        assertThat(result.message()).isEqualTo(res);
        assertThat(result.header()).isEqualTo(head);
        assertThat(result.closeConnection()).isFalse();
        assertThat(result.drop()).isFalse();
    }

    @Test
    void forwardRejectsRequestData() {
        var req = new FetchRequestData();
        var head = new ResponseHeaderData();
        assertThatThrownBy(() -> builder.forward(head, req)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void bareCloseConnection() {
        var result = builder.withCloseConnection2(true).build();
        assertThat(result.closeConnection()).isTrue();
    }

    @Test
    void forwardResponseWithCloseConnection() {
        var res = new FetchResponseData();
        var head = new ResponseHeaderData();
        var result = builder.forward(head, res).withCloseConnection2(true).build();
        assertThat(result.message()).isEqualTo(res);
        assertThat(result.header()).isEqualTo(head);
        assertThat(result.closeConnection()).isTrue();
    }

    @Test
    void drop() {
        var result = builder.drop().build();
        assertThat(result.drop()).isTrue();
        assertThat(result.message()).isNull();
        assertThat(result.header()).isNull();
    }

}
