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
import jakarta.json.JsonValue;
import jakarta.json.bind.annotation.JsonbProperty;
import lombok.Builder;

/**
 * Causes a Map State to write its results to the resource identified by
 * {@code resource} instead of returning them inline.  When present, the Map
 * State's result is an interpreter-defined JSON object rather than the
 * array of iteration outputs.
 *
 * @param resource URI uniquely identifying the task that writes the results; required
 * @param arguments input to the writer task; a JSON object or a JSONata string evaluating to one
 * @see <a href="https://states-language.net/spec.html#writing-results">Writing Results</a>
 */
@Builder(toBuilder = true, builderClassName = "Builder")
public record ResultWriter(@JsonbProperty("Resource") String resource,
                           @JsonbProperty("Arguments") JsonValue arguments) {
    public ResultWriter {
        ValidCheck.requireNotNull(resource, "resource");
    }
}
