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

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTypeAdapter;
import lombok.Builder;

/**
 * Terminates the state machine successfully, or ends a branch of a Parallel
 * State, or ends an iteration of a Map State.  A Succeed State is terminal
 * and may not assign variables.
 *
 * @param comment human-readable description of the state
 * @param queryLanguage overrides the state machine's query language; this model targets JSONata
 * @param output the state output; a JSON value or a JSONata string, defaults to the state input
 * @see <a href="https://states-language.net/spec.html#succeed-state">Succeed State</a>
 */
@Builder(toBuilder = true, builderClassName = "Builder")
public record SucceedState(@JsonbProperty("Comment") String comment,
                           @JsonbProperty("QueryLanguage") String queryLanguage,
                           @JsonbProperty("Output") @JsonbTypeAdapter(Output.Adapter.class) Output output) implements State {
}
