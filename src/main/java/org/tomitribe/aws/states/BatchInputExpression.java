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
import jakarta.json.JsonValue;
import lombok.Builder;

/**
 * The expression form of "BatchInput": a single JSONata expression
 * producing the fixed input merged into each batch an ItemBatcher creates.
 *
 * The expression is held without its "{%" and "%}" delimiters; they are
 * supplied on serialization and stripped on deserialization.
 *
 * @param expression the bare expression, without the "{%" and "%}" delimiters
 * @see <a href="https://states-language.net/spec.html#batching-items">Batching Items</a>
 */
@Builder(toBuilder = true, builderClassName = "Builder")
public record BatchInputExpression(String expression) implements BatchInput {

    public BatchInputExpression {
        ValidCheck.requireNotNull(expression, "expression");
        if (Expressions.isDelimited(expression)) {
            throw new IllegalArgumentException(String.format(
                    "Expression is already delimited: \"%s\"."
                            + "  Pass the expression without the {%% %%} delimiters",
                    expression));
        }
    }

    @Override
    public JsonValue toJsonValue() {
        return Json.createValue(Expressions.delimit(expression));
    }
}
