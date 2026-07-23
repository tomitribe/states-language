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
 * Passes its input to its output, performing no work.  Useful for injecting
 * fixed data, reshaping the payload with {@code output}, or assigning
 * variables with {@code assign}.
 *
 * @param comment human-readable description of the state
 * @param queryLanguage overrides the state machine's query language; this model targets JSONata
 * @param output the state output; a JSON value or a JSONata string, defaults to the state input
 * @param assign variable assignments; values may be JSONata strings, effective in the next state
 * @param next name of the state to transition to; exactly one of next or end is required
 * @param end true marks this state as terminal
 * @see <a href="https://states-language.net/spec.html#pass-state">Pass State</a>
 */
@Builder(toBuilder = true, builderClassName = "Builder")
public record PassState(@JsonbProperty("Comment") String comment,
                        @JsonbProperty("QueryLanguage") String queryLanguage,
                        @JsonbProperty("Output") @JsonbTypeAdapter(Output.Adapter.class) Output output,
                        @JsonbProperty("Assign") @JsonbTypeAdapter(Assign.Adapter.class) Assign assign,
                        @JsonbProperty("Next") String next,
                        @JsonbProperty("End") Boolean end) implements State {
    public PassState {
        Rules.requireTransition(PassState.class, next, end);
    }
}
