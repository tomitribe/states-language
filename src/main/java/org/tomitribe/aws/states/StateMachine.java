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
import jakarta.json.bind.annotation.JsonbTransient;
import lombok.Builder;

import java.util.Map;

/**
 * The top level of an Amazon States Language document.  Execution begins at
 * the state named by {@code startAt} and proceeds state to state until a
 * terminal state (Succeed, Fail, or any state with {@code "End": true}) is
 * reached or a runtime error occurs.
 *
 * @param comment human-readable description of the machine
 * @param queryLanguage the query language for the machine; this model targets "JSONata"
 * @param version the version of the States Language, "1.0" if omitted
 * @param timeoutSeconds maximum seconds the machine may execute before failing with States.Timeout
 * @param startAt name of the state execution begins at; must exactly match a key in {@code states}
 * @param states the states of the machine, keyed by state name (80 character maximum, unique)
 * @see <a href="https://states-language.net/spec.html#toplevelfields">Top-level fields</a>
 */
@Builder(toBuilder = true, builderClassName = "Builder")
public record StateMachine(@JsonbProperty("Comment") String comment,
                           @JsonbProperty("QueryLanguage") String queryLanguage,
                           @JsonbProperty("Version") String version,
                           @JsonbProperty("TimeoutSeconds") Integer timeoutSeconds,
                           @JsonbProperty("StartAt") String startAt,
                           @JsonbProperty("States") Map<String, State> states) {
    public StateMachine {
        ValidCheck.requireNotNull(startAt, "startAt");
        ValidCheck.requireNotNull(states, "states");
        states.keySet().forEach(Names::requireValidStateName);
    }

    /**
     * Checks the whole-document rules the record constructors cannot:
     * transition targets resolve within their own scope, state names are
     * unique across the entire machine, variables do not shadow outer
     * scopes, the effective query language is JSONata, and the
     * runtime-risk warnings.  Chain {@link Validation#requireValid()} to
     * fail fast: {@code machine.validate().requireValid()}
     */
    @JsonbTransient
    public Validation validate() {
        return Validations.validate(this);
    }
}
