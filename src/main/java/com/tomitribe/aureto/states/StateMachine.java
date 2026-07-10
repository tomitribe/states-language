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
    }
}
