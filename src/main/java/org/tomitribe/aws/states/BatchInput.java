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
 * The value of an ItemBatcher's "BatchInput" field: fixed input merged
 * into each batch before it is passed to the ItemProcessor.  The spec
 * allows a JSON text or a JSONata string that evaluates to one, so
 * BatchInput takes exactly two forms: {@link BatchInputExpression}, a
 * single JSONata expression producing the entire value, and
 * {@link BatchInputObject}, a JSON object of named fields.
 *
 * Serializes as the delimited expression string or the plain JSON object.
 *
 * @see <a href="https://states-language.net/spec.html#batching-items">Batching Items</a>
 */
public sealed interface BatchInput permits BatchInputObject, BatchInputExpression {

    JsonValue toJsonValue();

    static BatchInputExpression expression(final String expression) {
        return new BatchInputExpression(expression);
    }

    static BatchInputObject.Builder builder() {
        return BatchInputObject.builder();
    }

    /**
     * Serializes a BatchInput as the delimited expression string or the
     * plain JSON object it represents, restoring the correct form on
     * deserialization
     */
    class Adapter extends UnionAdapter<BatchInput, JsonObject> {

        public Adapter() {
            super("BatchInput", "object", JsonObject.class,
                    BatchInputObject::new, BatchInputExpression::new, BatchInput::toJsonValue);
        }
    }
}
