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

import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.bind.adapter.JsonbAdapter;

/**
 * The value of a Choice Rule's "Condition" field: the spec accepts a
 * boolean value or a JSONata string that must evaluate to one, so
 * Condition takes exactly two forms: {@link ConditionExpression}, a
 * JSONata expression, and {@link ConditionBoolean}, a fixed boolean.
 *
 * Serializes as the delimited expression string or the JSON boolean.
 *
 * @see <a href="https://states-language.net/spec.html#jsonata-choice-rules">JSONata Choice Rules</a>
 */
public sealed interface Condition permits ConditionBoolean, ConditionExpression {

    JsonValue toJsonValue();

    static ConditionExpression expression(final String expression) {
        return new ConditionExpression(expression);
    }

    static ConditionBoolean of(final boolean value) {
        return new ConditionBoolean(value);
    }

    /**
     * Serializes a Condition as the delimited expression string or the
     * JSON boolean it represents, restoring the correct form on
     * deserialization.  JSON-P has no boolean type — true and false are
     * JsonValue constants — so this adapter does not extend UnionAdapter,
     * whose structural arm is class-based.
     */
    class Adapter implements JsonbAdapter<Condition, JsonValue> {

        @Override
        public JsonValue adaptToJson(final Condition condition) {
            return condition.toJsonValue();
        }

        @Override
        public Condition adaptFromJson(final JsonValue value) {
            if (value.getValueType() == JsonValue.ValueType.TRUE) return new ConditionBoolean(true);
            if (value.getValueType() == JsonValue.ValueType.FALSE) return new ConditionBoolean(false);
            if (value instanceof JsonString string && Expressions.isDelimited(string.getString())) {
                return new ConditionExpression(Expressions.strip(string.getString()));
            }
            throw new IllegalArgumentException(String.format(
                    "Condition must be a boolean or a JSONata expression string"
                            + " delimited by {%% %%}.  Found %s: %s",
                    value.getValueType(), value));
        }
    }
}
