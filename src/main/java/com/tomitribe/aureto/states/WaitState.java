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
 * Delays the machine for a duration in seconds or until an absolute expiry
 * time.  Exactly one of {@code seconds} or {@code timestamp} must be
 * provided.
 *
 * Timestamps must conform to the RFC3339 profile of ISO 8601 with an
 * uppercase "T" separating date and time and an uppercase "Z" in the absence
 * of a numeric offset, for example "2016-03-14T01:59:00Z".
 *
 * @param comment human-readable description of the state
 * @param queryLanguage overrides the state machine's query language; this model targets JSONata
 * @param seconds non-negative integer wait duration
 * @param timestamp absolute expiry time, or a JSONata string evaluating to one
 * @param output the state output; a JSON value or a JSONata string, defaults to the state input
 * @param assign variable assignments; values may be JSONata strings, effective in the next state
 * @param next name of the state to transition to; exactly one of next or end is required
 * @param end true marks this state as terminal
 * @see <a href="https://states-language.net/spec.html#wait-state">Wait State</a>
 */
@Builder(toBuilder = true)
public record WaitState(@JsonbProperty("Comment") String comment,
                        @JsonbProperty("QueryLanguage") String queryLanguage,
                        @JsonbProperty("Seconds") Integer seconds,
                        @JsonbProperty("Timestamp") String timestamp,
                        @JsonbProperty("Output") JsonValue output,
                        @JsonbProperty("Assign") JsonObject assign,
                        @JsonbProperty("Next") String next,
                        @JsonbProperty("End") Boolean end) implements State {
}
