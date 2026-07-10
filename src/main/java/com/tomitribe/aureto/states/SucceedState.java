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

import jakarta.json.JsonValue;
import jakarta.json.bind.annotation.JsonbProperty;
import lombok.Builder;

/**
 * Terminates the state machine successfully, or ends a branch of a Parallel
 * State, or ends an iteration of a Map State.  A Succeed State is terminal
 * and may not assign variables.
 *
 * @param comment human-readable description of the state
 * @param output the state output; a JSON value or a JSONata string, defaults to the state input
 * @see <a href="https://states-language.net/spec.html#succeed-state">Succeed State</a>
 */
@Builder(toBuilder = true)
public record SucceedState(@JsonbProperty("Comment") String comment,
                           @JsonbProperty("Output") JsonValue output) implements State {
}
