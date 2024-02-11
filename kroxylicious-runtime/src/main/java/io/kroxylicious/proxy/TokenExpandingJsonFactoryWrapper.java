/*
 * Copyright Kroxylicious Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */

package io.kroxylicious.proxy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import org.apache.commons.text.StringSubstitutor;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.InputDecorator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ValueNode;

import edu.umd.cs.findbugs.annotations.NonNull;

public final class TokenExpandingJsonFactoryWrapper {

    private TokenExpandingJsonFactoryWrapper() {
    }

    /**
     * create a new JsonFactory will is guaranteed to be of the same type and have the
     * same configuration of the input factory.  The returned factory will perform
     * token expansion on the TextNodes of any input passed to the parser.
     */
    public static <F extends JsonFactory> F wrap(@NonNull F input) {
        var preprocessor = createPreprocessingObjectMapper(input);

        var builder = input.rebuild();
        builder.inputDecorator(new TokenExpandingInputDecorator(preprocessor));
        return (F) builder.build();
    }

    private static <F extends JsonFactory> ObjectMapper createPreprocessingObjectMapper(@NonNull F factory) {
        var preprocessingFactory = factory.rebuild().build();
        // https://github.com/FasterXML/jackson-databind/issues/2809 the preprocessor has to perform duplicate check. The duplicate
        // will be gone before the input reaches the next stage.
        return new ObjectMapper(preprocessingFactory)
                .configure(JsonParser.Feature.STRICT_DUPLICATE_DETECTION, true);
    }

    private static class TokenExpandingInputDecorator extends InputDecorator {
        private final ObjectMapper preprocessingMapper;

        protected TokenExpandingInputDecorator(ObjectMapper preprocessingMapper) {
            this.preprocessingMapper = preprocessingMapper;
        }

        @Override
        public Reader decorate(IOContext ctxt, Reader r) throws IOException {
            var tree = preprocessingMapper.readTree(r);
            var newTree = expandTokensInAllTextNodes(tree);
            return new StringReader(preprocessingMapper.writeValueAsString(newTree));
        }

        @Override
        public InputStream decorate(IOContext ctxt, InputStream in) throws IOException {
            var tree = preprocessingMapper.readTree(in);
            var newTree = expandTokensInAllTextNodes(tree);
            return new ByteArrayInputStream(preprocessingMapper.writeValueAsBytes(newTree));
        }

        @Override
        public InputStream decorate(IOContext ctxt, byte[] src, int offset, int length) throws IOException {
            var tree = preprocessingMapper.readTree(src, offset, length);
            var newTree = expandTokensInAllTextNodes(tree);
            return new ByteArrayInputStream(preprocessingMapper.writeValueAsBytes(newTree));
        }

        private JsonNode expandTokensInAllTextNodes(JsonNode tree) {
            var updatingTreeWalker = new ValueNodeUpdatingTreeWalker<>(TextNode.class::isInstance, TokenExpandingInputDecorator::expandTokensInTextNodes);
            return updatingTreeWalker.walkTree(tree);
        }

        private static TextNode expandTokensInTextNodes(TextNode current) {
            // this is where we will plug in our env var/sys property replacement.

            // this POC uses Apache commons-text for illustrative purposes. I don't want
            // Kroxylicious to have a commons-text dependency. StringSubstitutor's is very capable, but that
            // makes the attack surface large. I worry about CVEs.

            var text = current.asText();
            var replacement = StringSubstitutor.createInterpolator().replace(text);
            return text.equals(replacement) ? current : new TextNode(replacement);
        }
    }

    private record ValueNodeUpdatingTreeWalker<V extends ValueNode>(Predicate<ValueNode> valueSelector, UnaryOperator<V> valueReplacer) {

        public JsonNode walkTree(JsonNode tree) {
            var returnedTree = new AtomicReference<>(tree);
            walker(tree, returnedTree::set);
            return returnedTree.get();
        }

        private void walker(JsonNode node, Consumer<V> replacer) {
            if (node instanceof ObjectNode objectNode) {
                node.fields().forEachRemaining(e -> walker(e.getValue(), v -> objectNode.replace(e.getKey(), v)));
            }
            else if (node instanceof ArrayNode arrayNode) {
                for (int i = 0; i < arrayNode.size(); i++) {
                    int index = i;
                    walker(arrayNode.get(i), v -> arrayNode.set(index, v));
                }
            }
            else if (node instanceof ValueNode value) {
                if (valueSelector.test(value)) {
                    var replacement = this.valueReplacer.apply((V) value);
                    if (!replacement.equals(node)) {
                        replacer.accept(replacement);
                    }
                }
            }
            else {
                throw new IllegalStateException("unexpected node type :" + node);
            }
        }
    }
}
