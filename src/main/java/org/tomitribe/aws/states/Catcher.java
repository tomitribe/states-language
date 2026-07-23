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
import lombok.Singular;

import java.util.List;

/**
 * A fallback transition for a Task, Parallel or Map state.  When the state
 * reports an error that no Retrier resolves, the interpreter scans the
 * Catchers in order and transitions to {@code next} on the first match.
 *
 * If {@code output} is absent the state output is the Error Output, a JSON
 * object with an "Error" field naming the error and typically a "Cause"
 * field with human-readable detail.  The {@code output} and {@code assign}
 * fields may reference $states.errorOutput to combine the error with the
 * original state input.
 *
 * Note that error name matching is exact and case-sensitive.  The reserved
 * name "States.ALL" matches any error and must appear alone in the last
 * Catcher.
 *
 * @param errorEquals error names this Catcher matches; required, non-empty;
 *                    the predefined names are constants on {@link Errors}
 * @param output the state output; a JSON value or a JSONata string, may reference $states.errorOutput
 * @param assign variable assignments evaluated instead of the state's top-level assign
 * @param next name of the state to transition to; required
 * @see <a href="https://states-language.net/spec.html#fallback-states">Fallback states</a>
 */
@Builder(toBuilder = true, builderClassName = "Builder")
public record Catcher(@JsonbProperty("ErrorEquals") @Singular("error") List<String> errorEquals,
                      @JsonbProperty("Output") @JsonbTypeAdapter(Output.Adapter.class) Output output,
                      @JsonbProperty("Assign") @JsonbTypeAdapter(Assign.Adapter.class) Assign assign,
                      @JsonbProperty("Next") String next) {
    public Catcher {
        errorEquals = errorEquals == null || errorEquals.isEmpty() ? null : List.copyOf(errorEquals);
        ValidCheck.requireNotNull(errorEquals, "errorEquals");
        ValidCheck.requireNotNull(next, "next");
    }
}
