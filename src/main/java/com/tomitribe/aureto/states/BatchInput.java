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
 * The value of an ItemBatcher's "BatchInput" field: fixed input merged
 * into each batch before it is passed to the ItemProcessor.  The spec
 * allows a JSON text or a JSONata string that evaluates to one, so
 * BatchInput takes exactly two forms: {@link BatchInputExpression}, a
 * single JSONata expression producing the entire value, and
 * {@link BatchInputObject}, a JSON object of named fields.
 *
 * Serializes as the delimited expression string or the plain JSON object.
 *
 * @see <a href="https://states-language.net/spec.html#batching-items">Batching Items</a>
 */
public sealed interface BatchInput permits BatchInputObject, BatchInputExpression {

    JsonValue toJsonValue();

    static BatchInputExpression expression(final String expression) {
        return new BatchInputExpression(expression);
    }

    static BatchInputObject.Builder builder() {
        return BatchInputObject.builder();
    }

    /**
     * Serializes a BatchInput as the delimited expression string or the
     * plain JSON object it represents, restoring the correct form on
     * deserialization
     */
    class Adapter extends UnionAdapter<BatchInput, JsonObject> {

        public Adapter() {
            super("BatchInput", "object", JsonObject.class,
                    BatchInputObject::new, BatchInputExpression::new, BatchInput::toJsonValue);
        }
    }
}
