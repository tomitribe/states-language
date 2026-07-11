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

import java.net.URI;

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
public record ResultWriter(@JsonbProperty("Resource") URI resource,
                           @JsonbProperty("Arguments") @JsonbTypeAdapter(Arguments.Adapter.class) Arguments arguments) {
    public ResultWriter {
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
