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
 * Executes each branch concurrently and waits for all branches to terminate
 * before transitioning.  The result is an array with one element per branch,
 * in declaration order, containing that branch's output.
 *
 * If any branch fails, the entire Parallel State fails and all branches are
 * terminated.  A Succeed State inside a branch merely ends its own branch.
 *
 * @param comment human-readable description of the state
 * @param queryLanguage overrides the state machine's query language; this model targets JSONata
 * @param branches the branches to execute concurrently; required, non-empty
 * @param arguments input to each branch's StartAt state; a JSON value or a JSONata string
 * @param output the state output; a JSON value or a JSONata string, may reference $states.result
 * @param assign variable assignments; values may be JSONata strings, effective in the next state
 * @param retry retry policies scanned in order when the state reports an error
 * @param catchers fallback transitions scanned in order when retries are exhausted
 * @param next name of the state to transition to; exactly one of next or end is required
 * @param end true marks this state as terminal
 * @see <a href="https://states-language.net/spec.html#parallel-state">Parallel State</a>
 */
@Builder(toBuilder = true, builderClassName = "Builder")
public record ParallelState(@JsonbProperty("Comment") String comment,
                            @JsonbProperty("QueryLanguage") String queryLanguage,
                            @JsonbProperty("Branches") @Singular List<Branch> branches,
                            @JsonbProperty("Arguments") @JsonbTypeAdapter(Arguments.Adapter.class) Arguments arguments,
                            @JsonbProperty("Output") @JsonbTypeAdapter(Output.Adapter.class) Output output,
                            @JsonbProperty("Assign") @JsonbTypeAdapter(Assign.Adapter.class) Assign assign,
                            @JsonbProperty("Retry") @Singular("retrier") List<Retrier> retry,
                            @JsonbProperty("Catch") @Singular("catcher") List<Catcher> catchers,
                            @JsonbProperty("Next") String next,
                            @JsonbProperty("End") Boolean end) implements State {
    public ParallelState {
        branches = branches == null || branches.isEmpty() ? null : List.copyOf(branches);
        ValidCheck.requireNotNull(branches, "branches");
        Rules.requireTransition(ParallelState.class, next, end);
        retry = retry == null || retry.isEmpty() ? null : List.copyOf(retry);
        catchers = catchers == null || catchers.isEmpty() ? null : List.copyOf(catchers);
        Rules.requireStatesAllPlacement(retry, catchers);
    }
}
