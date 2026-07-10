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

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.json.bind.annotation.JsonbProperty;
import lombok.Builder;

/**
 * Passes its input to its output, performing no work.  Useful for injecting
 * fixed data, reshaping the payload with {@code output}, or assigning
 * variables with {@code assign}.
 *
 * @param comment human-readable description of the state
 * @param queryLanguage overrides the state machine's query language; this model targets JSONata
 * @param output the state output; a JSON value or a JSONata string, defaults to the state input
 * @param assign variable assignments; values may be JSONata strings, effective in the next state
 * @param next name of the state to transition to; exactly one of next or end is required
 * @param end true marks this state as terminal
 * @see <a href="https://states-language.net/spec.html#pass-state">Pass State</a>
 */
@Builder(toBuilder = true)
public record PassState(@JsonbProperty("Comment") String comment,
                        @JsonbProperty("QueryLanguage") String queryLanguage,
                        @JsonbProperty("Output") JsonValue output,
                        @JsonbProperty("Assign") JsonObject assign,
                        @JsonbProperty("Next") String next,
                        @JsonbProperty("End") Boolean end) implements State {
}
