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
import jakarta.json.bind.adapter.JsonbAdapter;
import jakarta.json.bind.annotation.JsonbTypeAdapter;

/**
 * The value of an "Assign" field: variable names mapped to the values they
 * will hold in the next state.  Each value is either a JSON literal or a
 * JSONata expression, a string delimited by "{%" and "%}".  This class owns
 * that duality: the builder's {@code value} methods add literals and its
 * {@code expression} method adds expressions, supplying the delimiters so
 * callers never write them; the {@code isExpression} and
 * {@code getExpression} methods read them back out.
 *
 * Variable names are validated as they are added: at most 80 Unicode
 * characters, not the reserved name "states", and a valid Unicode
 * identifier.
 *
 * Serializes as the plain JSON object, for example
 * <pre>
 * "Assign": {
 *   "product": "{% $states.input.product %}",
 *   "attempts": 0
 * }
 * </pre>
 *
 * @param variables the assigned variables, name to value
 * @see <a href="https://states-language.net/spec.html#assigning-variables">Assigning State Machine Variables</a>
 * @see <a href="https://states-language.net/spec.html#expressions">JSONata Expressions</a>
 */
@JsonbTypeAdapter(Assign.Adapter.class)
public record Assign(JsonObject variables) implements Values {

    public Assign {
        ValidCheck.requireNotNull(variables, "variables");
        variables.keySet().forEach(Names::requireValidVariableName);
    }

    @Override
    public JsonObject toJsonObject() {
        return variables;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder().putAll(variables);
    }

    public static class Builder extends ObjectBuilder<Builder, Assign> {

        Builder() {
            super("variable");
        }

        @Override
        public Assign build() {
            return new Assign(toJsonObject());
        }

        @Override
        protected String checkName(final String name) {
            return Names.requireValidVariableName(name);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    /**
     * Serializes an Assign as the plain JSON object it wraps
     */
    public static class Adapter implements JsonbAdapter<Assign, JsonObject> {

        @Override
        public JsonObject adaptToJson(final Assign assign) {
            return assign.variables();
        }

        @Override
        public Assign adaptFromJson(final JsonObject variables) {
            return new Assign(variables);
        }
    }
}
