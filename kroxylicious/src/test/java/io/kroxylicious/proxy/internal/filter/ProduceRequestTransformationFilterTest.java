/*
 * Copyright Kroxylicious Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */

package io.kroxylicious.proxy.internal.filter;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.kroxylicious.proxy.filter.FilterConstructContext;
import io.kroxylicious.proxy.service.ConfigurationDefinition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ProduceRequestTransformationFilterTest {
    @Test
    void testContributor() {
        ProduceRequestTransformationFilter.Contributor contributor = new ProduceRequestTransformationFilter.Contributor();
        assertThat(contributor.getTypeName()).isEqualTo("ProduceRequestTransformation");
        assertThat(contributor.getConfigDefinition()).isEqualTo(
                new ConfigurationDefinition(ProduceRequestTransformationFilter.ProduceRequestTransformationConfig.class, true));
        FilterConstructContext constructContext = Mockito.mock(FilterConstructContext.class);
        when(constructContext.getConfig()).thenReturn(
                new ProduceRequestTransformationFilter.ProduceRequestTransformationConfig(ProduceRequestTransformationFilter.UpperCasing.class.getName()));
        assertThat(contributor.getInstance(constructContext)).isInstanceOf(ProduceRequestTransformationFilter.class);
    }
}