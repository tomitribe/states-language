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

import jakarta.json.bind.annotation.JsonbProperty;
import lombok.Builder;

/**
 * Terminates the state machine and marks it as a failure.  A Fail State is
 * terminal and may not assign variables or reshape output.
 *
 * @param comment human-readable description of the state
 * @param error error name for Retry/Catch or diagnostic purposes, or a JSONata string evaluating to one
 * @param cause human-readable failure message, or a JSONata string evaluating to one
 * @see <a href="https://states-language.net/spec.html#fail-state">Fail State</a>
 */
@Builder(toBuilder = true)
public record FailState(@JsonbProperty("Comment") String comment,
                        @JsonbProperty("Error") String error,
                        @JsonbProperty("Cause") String cause) implements State {
}
