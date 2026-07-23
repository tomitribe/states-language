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
 * The value of an "Arguments" field: the input to the task a Task State
 * executes, or to the branches of a Parallel State.  The spec allows a JSON
 * text or a JSONata string that evaluates to one, so Arguments takes
 * exactly two forms: {@link ArgumentsExpression}, a single JSONata
 * expression producing the entire value, and {@link ArgumentsObject}, a
 * JSON object of named arguments.
 *
 * Serializes as the delimited expression string or the plain JSON object.
 *
 * @see <a href="https://states-language.net/spec.html#arguments-and-output">Using Arguments and Output</a>
 */
public sealed interface Arguments permits ArgumentsObject, ArgumentsExpression {

    JsonValue toJsonValue();

    static ArgumentsExpression expression(final String expression) {
        return new ArgumentsExpression(expression);
    }

    static ArgumentsObject.Builder builder() {
        return ArgumentsObject.builder();
    }

    /**
     * Serializes an Arguments as the delimited expression string or the
     * plain JSON object it represents, restoring the correct form on
     * deserialization
     */
    class Adapter extends UnionAdapter<Arguments, JsonObject> {

        public Adapter() {
            super("Arguments", "object", JsonObject.class,
                    ArgumentsObject::new, ArgumentsExpression::new, Arguments::toJsonValue);
        }
    }
}
