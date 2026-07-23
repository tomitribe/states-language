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

import jakarta.json.JsonArray;
import jakarta.json.JsonValue;

/**
 * The value of a Map State's "Items" field: the array whose elements the
 * ItemProcessor runs once per item, called the Items Array.  The spec
 * allows a JSON array or a JSONata string that must produce one, so Items
 * takes exactly two forms: {@link ItemsExpression}, a single JSONata
 * expression producing the entire array, and {@link ItemsArray}, the array
 * given inline.
 *
 * Serializes as the delimited expression string or the plain JSON array.
 *
 * @see <a href="https://states-language.net/spec.html#selecting-items">Selecting Items</a>
 */
public sealed interface Items permits ItemsArray, ItemsExpression {

    JsonValue toJsonValue();

    static ItemsExpression expression(final String expression) {
        return new ItemsExpression(expression);
    }

    static ItemsArray.Builder builder() {
        return ItemsArray.builder();
    }

    /**
     * Serializes an Items as the delimited expression string or the plain
     * JSON array it represents, restoring the correct form on
     * deserialization
     */
    class Adapter extends UnionAdapter<Items, JsonArray> {

        public Adapter() {
            super("Items", "array", JsonArray.class,
                    ItemsArray::new, ItemsExpression::new, Items::toJsonValue);
        }
    }
}
