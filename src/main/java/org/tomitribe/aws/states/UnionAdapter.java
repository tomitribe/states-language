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

import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.bind.adapter.JsonbAdapter;

import java.util.function.Function;

/**
 * Serializes the model's structure-or-expression unions — Arguments and
 * Output, whose structural form is a JSON object, and Items, whose
 * structural form is a JSON array — and restores the correct form on
 * deserialization: the structural type becomes the structural form, a
 * "{%"-delimited string becomes the expression form with the delimiters
 * stripped.  Any other JSON value fails with a message naming the field; a
 * bare string in particular is almost certainly an expression missing its
 * delimiters.
 *
 * @param <A> the union interface the adapter serves
 * @param <J> the JSON type of the union's structural form
 */
class UnionAdapter<A, J extends JsonValue> implements JsonbAdapter<A, JsonValue> {

    private final String field;
    private final String structureNoun;
    private final Class<J> structure;
    private final Function<J, A> structures;
    private final Function<String, A> expressions;
    private final Function<A, JsonValue> json;

    UnionAdapter(final String field,
                 final String structureNoun,
                 final Class<J> structure,
                 final Function<J, A> structures,
                 final Function<String, A> expressions,
                 final Function<A, JsonValue> json) {
        this.field = field;
        this.structureNoun = structureNoun;
        this.structure = structure;
        this.structures = structures;
        this.expressions = expressions;
        this.json = json;
    }

    @Override
    public JsonValue adaptToJson(final A value) {
        return json.apply(value);
    }

    @Override
    public A adaptFromJson(final JsonValue value) {
        if (structure.isInstance(value)) return structures.apply(structure.cast(value));
        if (value instanceof JsonString string && Expressions.isDelimited(string.getString())) {
            return expressions.apply(Expressions.strip(string.getString()));
        }
        throw new IllegalArgumentException(String.format(
                "%s must be a JSON %s or a JSONata expression string"
                        + " delimited by {%% %%}.  Found %s: %s",
                field, structureNoun, value.getValueType(), value));
    }
}
