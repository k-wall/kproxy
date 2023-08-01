/*
 * Copyright Kroxylicious Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */

package io.kroxylicious.proxy.filter;

import java.util.concurrent.CompletionStage;

public interface TerminalStage<FR extends FilterResult> {
    FR build();

    CompletionStage<FR> completedFilterResult();
}
