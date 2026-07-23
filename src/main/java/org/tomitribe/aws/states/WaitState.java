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
 * Delays the machine for a duration in seconds or until an absolute expiry
 * time.  Exactly one of {@code seconds} or {@code timestamp} must be
 * provided.
 *
 * Timestamps must conform to the RFC3339 profile of ISO 8601 with an
 * uppercase "T" separating date and time and an uppercase "Z" in the absence
 * of a numeric offset, for example "2016-03-14T01:59:00Z".
 *
 * @param comment human-readable description of the state
 * @param queryLanguage overrides the state machine's query language; this model targets JSONata
 * @param seconds non-negative integer wait duration
 * @param timestamp absolute expiry time, or a JSONata string evaluating to one
 * @param output the state output; a JSON value or a JSONata string, defaults to the state input
 * @param assign variable assignments; values may be JSONata strings, effective in the next state
 * @param next name of the state to transition to; exactly one of next or end is required
 * @param end true marks this state as terminal
 * @see <a href="https://states-language.net/spec.html#wait-state">Wait State</a>
 */
@Builder(toBuilder = true, builderClassName = "Builder")
public record WaitState(@JsonbProperty("Comment") String comment,
                        @JsonbProperty("QueryLanguage") String queryLanguage,
                        @JsonbProperty("Seconds") Integer seconds,
                        @JsonbProperty("Timestamp") String timestamp,
                        @JsonbProperty("Output") @JsonbTypeAdapter(Output.Adapter.class) Output output,
                        @JsonbProperty("Assign") @JsonbTypeAdapter(Assign.Adapter.class) Assign assign,
                        @JsonbProperty("Next") String next,
                        @JsonbProperty("End") Boolean end) implements State {
    public WaitState {
        Rules.requireTransition(WaitState.class, next, end);
        if ((seconds == null) == (timestamp == null)) {
            throw new IllegalArgumentException(
                    "WaitState must have exactly one of \"Seconds\" or \"Timestamp\"");
        }
        Rules.requireNonNegative("Seconds", seconds);
    }
}
