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
 * The object form of "Arguments": a JSON object of named arguments, each a
 * JSON literal or a JSONata expression.  Argument names are the task's own
 * contract, so unlike Assign no naming rules apply.
 *
 * Expressions may appear at any depth, so the builder composes: a nested
 * Arguments becomes a nested object or an expression evaluated in place.
 *
 * @param values the arguments, name to value; expressions are held in
 *               their delimited string form
 * @see <a href="https://states-language.net/spec.html#arguments-and-output">Using Arguments and Output</a>
 */
public record ArgumentsObject(JsonObject values) implements Arguments, Values {

    public ArgumentsObject {
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

    public static class Builder extends ObjectBuilder<Builder, ArgumentsObject> {

        Builder() {
            super("argument");
        }

        /**
         * Nests another Arguments as the named argument's value: an object
         * becomes a nested object, an expression becomes a delimited
         * string the interpreter evaluates in place
         */
        public Builder value(final String name, final Arguments value) {
            ValidCheck.requireNotNull(value, "value");
            return value(name, value.toJsonValue());
        }

        @Override
        public ArgumentsObject build() {
            return new ArgumentsObject(toJsonObject());
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
