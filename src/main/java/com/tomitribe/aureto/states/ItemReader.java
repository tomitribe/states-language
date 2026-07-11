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
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTypeAdapter;
import lombok.Builder;

import java.net.URI;

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
public record ItemReader(@JsonbProperty("Resource") URI resource,
                         @JsonbProperty("ReaderConfig") JsonObject readerConfig,
                         @JsonbProperty("Arguments") @JsonbTypeAdapter(Arguments.Adapter.class) Arguments arguments) {
    public ItemReader {
        ValidCheck.requireNotNull(resource, "resource");
    }

    public static class Builder {

        // Lombok skips generating any setter whose name is hand-written,
        // signatures notwithstanding, so declaring the String convenience
        // requires declaring the URI form as well
        public Builder resource(final URI resource) {
            this.resource = resource;
            return this;
        }

        /**
         * Convenience for the common case of a resource URI in hand as a
         * string, for example an ARN.  Fails immediately if the value is
         * not a syntactically valid URI.
         */
        public Builder resource(final String resource) {
            return resource(URI.create(resource));
        }
    }
}
