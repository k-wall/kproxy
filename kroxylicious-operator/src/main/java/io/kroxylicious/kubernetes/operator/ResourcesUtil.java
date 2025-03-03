/*
 * Copyright Kroxylicious Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */

package io.kroxylicious.kubernetes.operator;

import java.util.Comparator;
import java.util.stream.Stream;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.api.model.OwnerReferenceBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;

import io.kroxylicious.kubernetes.api.v1alpha1.KafkaClusterRef;
import io.kroxylicious.kubernetes.api.v1alpha1.KafkaProxy;
import io.kroxylicious.kubernetes.api.v1alpha1.VirtualKafkaCluster;

public class ResourcesUtil {
    private ResourcesUtil() {
    }

    static <O extends HasMetadata> OwnerReference ownerReferenceTo(O owner) {
        return new OwnerReferenceBuilder()
                .withKind(owner.getKind())
                .withApiVersion(owner.getApiVersion())
                .withName(owner.getMetadata().getName())
                .withUid(owner.getMetadata().getUid())
                .build();
    }

    static Stream<VirtualKafkaCluster> clustersInNameOrder(Context<KafkaProxy> context) {
        return context.getSecondaryResources(VirtualKafkaCluster.class)
                .stream().sorted(Comparator.comparing(virtualKafkaCluster -> virtualKafkaCluster.getMetadata().getName()));
    }

    // KWTODO - think I should be using a discriminator, so I only consider the refs I need to
    static Stream<KafkaClusterRef> clusterRefsInNameOrder(Context<KafkaProxy> context) {
        return context.getSecondaryResource(KafkaClusterRef.class)
                .stream().sorted(Comparator.comparing(clusterRef -> clusterRef.getMetadata().getName()));
    }

}
