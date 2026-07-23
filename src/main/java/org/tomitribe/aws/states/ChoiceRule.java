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
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTypeAdapter;
import lombok.Builder;

/**
 * A single rule in a Choice State's {@code choices} array.  In the JSONata
 * dialect the condition is a boolean literal or a JSONata string that must
 * evaluate to a boolean.
 *
 * If this rule is chosen, its {@code assign} and {@code output} fields are
 * evaluated and the state's top-level equivalents are not.
 *
 * @param comment human-readable description of the rule
 * @param condition boolean or JSONata string evaluating to a boolean; required
 * @param output the state output if this rule is chosen; a JSON value or a JSONata string
 * @param assign variable assignments evaluated if this rule is chosen
 * @param next name of the state to transition to if this rule is chosen; required
 * @see <a href="https://states-language.net/spec.html#jsonata-choice-rules">JSONata Choice Rules</a>
 */
@Builder(toBuilder = true, builderClassName = "Builder")
public record ChoiceRule(@JsonbProperty("Comment") String comment,
                         @JsonbProperty("Condition") @JsonbTypeAdapter(Condition.Adapter.class) Condition condition,
                         @JsonbProperty("Output") @JsonbTypeAdapter(Output.Adapter.class) Output output,
                         @JsonbProperty("Assign") @JsonbTypeAdapter(Assign.Adapter.class) Assign assign,
                         @JsonbProperty("Next") String next) {
    public ChoiceRule {
        ValidCheck.requireNotNull(condition, "condition");
        ValidCheck.requireNotNull(next, "next");
    }
}
