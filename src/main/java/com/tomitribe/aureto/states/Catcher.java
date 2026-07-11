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
 * @param errorEquals error names this Catcher matches; required, non-empty
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
