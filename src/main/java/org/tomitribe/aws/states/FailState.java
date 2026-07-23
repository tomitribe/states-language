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
import lombok.Builder;

/**
 * Terminates the state machine and marks it as a failure.  A Fail State is
 * terminal and may not assign variables or reshape output.
 *
 * @param comment human-readable description of the state
 * @param queryLanguage overrides the state machine's query language; this model targets JSONata
 * @param error error name for Retry/Catch or diagnostic purposes, or a JSONata string evaluating to one
 * @param cause human-readable failure message, or a JSONata string evaluating to one
 * @see <a href="https://states-language.net/spec.html#fail-state">Fail State</a>
 */
@Builder(toBuilder = true, builderClassName = "Builder")
public record FailState(@JsonbProperty("Comment") String comment,
                        @JsonbProperty("QueryLanguage") String queryLanguage,
                        @JsonbProperty("Error") String error,
                        @JsonbProperty("Cause") String cause) implements State {
}
