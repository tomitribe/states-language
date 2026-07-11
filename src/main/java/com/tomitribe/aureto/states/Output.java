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

/**
 * The value of an "Output" field: the state output, which becomes the input
 * of the next state.  The spec allows a JSON text or a JSONata string that
 * evaluates to one, so Output takes exactly two forms:
 * {@link OutputExpression}, a single JSONata expression producing the
 * entire value, and {@link OutputObject}, a JSON object of named fields.
 *
 * Serializes as the delimited expression string or the plain JSON object.
 *
 * @see <a href="https://states-language.net/spec.html#arguments-and-output">Using Arguments and Output</a>
 */
public sealed interface Output permits OutputObject, OutputExpression {

    JsonValue toJsonValue();

    static OutputExpression expression(final String expression) {
        return new OutputExpression(expression);
    }

    static OutputObject.Builder builder() {
        return OutputObject.builder();
    }

    /**
     * Serializes an Output as the delimited expression string or the plain
     * JSON object it represents, restoring the correct form on
     * deserialization
     */
    class Adapter extends UnionAdapter<Output, JsonObject> {

        public Adapter() {
            super("Output", "object", JsonObject.class,
                    OutputObject::new, OutputExpression::new, Output::toJsonValue);
        }
    }
}
