/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tomitribe.aws.states;

import io.github.aglibs.validcheck.ValidCheck;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonValue;

import java.util.ArrayList;
import java.util.List;

/**
 * The array form of "Items": the Items Array itself, given inline.  Each
 * element is a JSON literal or a JSONata expression evaluated in place,
 * so {@code [1, "{% $two %}", "three"]} is a legal Items Array.
 *
 * @param items the Items Array; expressions are held in their delimited string form
 * @see <a href="https://states-language.net/spec.html#selecting-items">Selecting Items</a>
 */
public record ItemsArray(JsonArray items) implements Items {

    public ItemsArray {
        ValidCheck.requireNotNull(items, "items");
    }

    @Override
    public JsonValue toJsonValue() {
        return items;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        final Builder builder = new Builder();
        builder.items.addAll(items);
        return builder;
    }

    public static class Builder {

        private final List<JsonValue> items = new ArrayList<>();

        public Builder item(final String value) {
            ValidCheck.requireNotNull(value, "value");
            if (Expressions.isDelimited(value)) {
                throw new IllegalArgumentException(String.format(
                        "Item looks like a JSONata expression: \"%s\"."
                                + "  Use expression(\"%s\") instead",
                        value, Expressions.strip(value)));
            }
            return item(Json.createValue(value));
        }

        public Builder item(final int value) {
            return item(Json.createValue(value));
        }

        public Builder item(final double value) {
            return item(Json.createValue(value));
        }

        public Builder item(final boolean value) {
            return item(value ? JsonValue.TRUE : JsonValue.FALSE);
        }

        public Builder item(final JsonValue value) {
            ValidCheck.requireNotNull(value, "value");
            items.add(value);
            return this;
        }

        /**
         * Adds a JSONata expression as the next item, wrapping it in the
         * "{%" and "%}" delimiters.  Pass the bare expression.
         */
        public Builder expression(final String expression) {
            ValidCheck.requireNotNull(expression, "expression");
            if (Expressions.isDelimited(expression)) {
                throw new IllegalArgumentException(String.format(
                        "Expression is already delimited: \"%s\"."
                                + "  Pass the expression without the {%% %%} delimiters",
                        expression));
            }
            return item(Json.createValue(Expressions.delimit(expression)));
        }

        public ItemsArray build() {
            final var json = Json.createArrayBuilder();
            items.forEach(json::add);
            return new ItemsArray(json.build());
        }
    }
}
