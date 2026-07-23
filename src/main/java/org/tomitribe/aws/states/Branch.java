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
import lombok.Builder;

import java.util.Map;

/**
 * A single branch of a Parallel State.  The {@code startAt} and
 * {@code states} fields work exactly like those at the top level of a state
 * machine.  States in a branch can transition only to each other, and no
 * state outside the branch can transition into it.
 *
 * @param startAt name of the state execution begins at; must exactly match a key in {@code states}
 * @param states the states of the branch, keyed by state name
 * @see <a href="https://states-language.net/spec.html#parallel-state">Parallel State</a>
 */
@Builder(toBuilder = true, builderClassName = "Builder")
public record Branch(@JsonbProperty("StartAt") String startAt,
                     @JsonbProperty("States") Map<String, State> states) {
    public Branch {
        ValidCheck.requireNotNull(startAt, "startAt");
        ValidCheck.requireNotNull(states, "states");
        states.keySet().forEach(Names::requireValidStateName);
    }
}
