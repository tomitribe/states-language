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
 * The value of an "Arguments" field: the input to the task a Task State
 * executes, or to the branches of a Parallel State.  The spec allows a JSON
 * text or a JSONata string that evaluates to one, so Arguments takes
 * exactly two forms: {@link ArgumentsExpression}, a single JSONata
 * expression producing the entire value, and {@link ArgumentsObject}, a
 * JSON object of named arguments.
 *
 * Serializes as the delimited expression string or the plain JSON object.
 *
 * @see <a href="https://states-language.net/spec.html#arguments-and-output">Using Arguments and Output</a>
 */
public sealed interface Arguments permits ArgumentsObject, ArgumentsExpression {

    JsonValue toJsonValue();

    static ArgumentsExpression expression(final String expression) {
        return new ArgumentsExpression(expression);
    }

    static ArgumentsObject.Builder builder() {
        return ArgumentsObject.builder();
    }

    /**
     * Serializes an Arguments as the delimited expression string or the
     * plain JSON object it represents, restoring the correct form on
     * deserialization
     */
    class Adapter extends UnionAdapter<Arguments, JsonObject> {

        public Adapter() {
            super("Arguments", "object", JsonObject.class,
                    ArgumentsObject::new, ArgumentsExpression::new, Arguments::toJsonValue);
        }
    }
}
