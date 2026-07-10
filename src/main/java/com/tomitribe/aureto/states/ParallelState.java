/*
 * Tomitribe Confidential
 *
 * Copyright Tomitribe Corporation. 2026
 *
 * The source code for this program is not published or otherwise divested
 * of its trade secrets, irrespective of what has been deposited with the
 * U.S. Copyright Office.
 */
package com.tomitribe.aureto.states;

import io.github.aglibs.validcheck.ValidCheck;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.json.bind.annotation.JsonbProperty;
import lombok.Builder;

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
@Builder(toBuilder = true)
public record ParallelState(@JsonbProperty("Comment") String comment,
                            @JsonbProperty("Branches") List<Branch> branches,
                            @JsonbProperty("Arguments") JsonValue arguments,
                            @JsonbProperty("Output") JsonValue output,
                            @JsonbProperty("Assign") JsonObject assign,
                            @JsonbProperty("Retry") List<Retrier> retry,
                            @JsonbProperty("Catch") List<Catcher> catchers,
                            @JsonbProperty("Next") String next,
                            @JsonbProperty("End") Boolean end) implements State {
    public ParallelState {
        ValidCheck.requireNotNull(branches, "branches");
    }
}
