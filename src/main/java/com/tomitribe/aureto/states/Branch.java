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
    }
}
