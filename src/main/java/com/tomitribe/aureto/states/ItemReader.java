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

/**
 * Causes a Map State to read its items from the resource identified by
 * {@code resource} instead of from the state input.  On AWS this is how a
 * Distributed Map reads items from S3.
 *
 * @param resource URI uniquely identifying the task that reads the items; required
 * @param readerConfig reader configuration; the spec defines "MaxItems", interpreters may define more
 * @param arguments input to the reader task; a JSON object or a JSONata string evaluating to one
 * @see <a href="https://states-language.net/spec.html#reading-items">Reading Items</a>
 */
@Builder(toBuilder = true, builderClassName = "Builder")
public record ItemReader(@JsonbProperty("Resource") String resource,
                         @JsonbProperty("ReaderConfig") JsonObject readerConfig,
                         @JsonbProperty("Arguments") JsonValue arguments) {
    public ItemReader {
        ValidCheck.requireNotNull(resource, "resource");
    }
}
