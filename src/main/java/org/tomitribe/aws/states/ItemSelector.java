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

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

/**
 * The value of a Map State's "ItemSelector" field: overrides each single
 * element of the Items Array to produce the selected item passed to the
 * ItemProcessor.  The spec allows a JSON text or a JSONata string that
 * evaluates to one, so ItemSelector takes exactly two forms:
 * {@link ItemSelectorExpression}, a single JSONata expression producing
 * the entire value, and {@link ItemSelectorObject}, a JSON object of named
 * fields.
 *
 * Serializes as the delimited expression string or the plain JSON object.
 *
 * @see <a href="https://states-language.net/spec.html#selecting-items">Selecting Items</a>
 */
public sealed interface ItemSelector permits ItemSelectorObject, ItemSelectorExpression {

    JsonValue toJsonValue();

    static ItemSelectorExpression expression(final String expression) {
        return new ItemSelectorExpression(expression);
    }

    static ItemSelectorObject.Builder builder() {
        return ItemSelectorObject.builder();
    }

    /**
     * Serializes an ItemSelector as the delimited expression string or the
     * plain JSON object it represents, restoring the correct form on
     * deserialization
     */
    class Adapter extends UnionAdapter<ItemSelector, JsonObject> {

        public Adapter() {
            super("ItemSelector", "object", JsonObject.class,
                    ItemSelectorObject::new, ItemSelectorExpression::new, ItemSelector::toJsonValue);
        }
    }
}
