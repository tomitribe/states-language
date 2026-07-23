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
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

/**
 * The object form of "ItemSelector": a JSON object whose field values are
 * JSON literals or JSONata expressions, overriding each element of the
 * Items Array.  Expressions typically reference the item being processed
 * through {@code $states.context.Map.Item.Value} and
 * {@code $states.context.Map.Item.Index}.
 *
 * @param values the selected item's fields, name to value; expressions are
 *               held in their delimited string form
 * @see <a href="https://states-language.net/spec.html#selecting-items">Selecting Items</a>
 */
public record ItemSelectorObject(JsonObject values) implements ItemSelector, Values {

    public ItemSelectorObject {
        ValidCheck.requireNotNull(values, "values");
    }

    @Override
    public JsonValue toJsonValue() {
        return values;
    }

    @Override
    public JsonObject toJsonObject() {
        return values;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder().putAll(values);
    }

    public static class Builder extends ObjectBuilder<Builder, ItemSelectorObject> {

        Builder() {
            super("field");
        }

        /**
         * Nests another ItemSelector as the named field's value: an object
         * becomes a nested object, an expression becomes a delimited
         * string the interpreter evaluates in place
         */
        public Builder value(final String name, final ItemSelector value) {
            ValidCheck.requireNotNull(value, "value");
            return value(name, value.toJsonValue());
        }

        @Override
        public ItemSelectorObject build() {
            return new ItemSelectorObject(toJsonObject());
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
