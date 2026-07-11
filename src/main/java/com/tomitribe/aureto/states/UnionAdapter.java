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
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.bind.adapter.JsonbAdapter;

import java.util.function.Function;

/**
 * Serializes the model's object-or-expression unions — Arguments, Output —
 * and restores the correct form on deserialization: a JSON object becomes
 * the object form, a "{%"-delimited string becomes the expression form with
 * the delimiters stripped.  Any other JSON value fails with a message
 * naming the field; a bare string in particular is almost certainly an
 * expression missing its delimiters.
 *
 * @param <A> the union interface the adapter serves
 */
class UnionAdapter<A> implements JsonbAdapter<A, JsonValue> {

    private final String field;
    private final Function<JsonObject, A> objects;
    private final Function<String, A> expressions;
    private final Function<A, JsonValue> json;

    UnionAdapter(final String field,
                 final Function<JsonObject, A> objects,
                 final Function<String, A> expressions,
                 final Function<A, JsonValue> json) {
        this.field = field;
        this.objects = objects;
        this.expressions = expressions;
        this.json = json;
    }

    @Override
    public JsonValue adaptToJson(final A value) {
        return json.apply(value);
    }

    @Override
    public A adaptFromJson(final JsonValue value) {
        if (value instanceof JsonObject object) return objects.apply(object);
        if (value instanceof JsonString string && Expressions.isDelimited(string.getString())) {
            return expressions.apply(Expressions.strip(string.getString()));
        }
        throw new IllegalArgumentException(String.format(
                "%s must be a JSON object or a JSONata expression string"
                        + " delimited by {%% %%}.  Found %s: %s",
                field, value.getValueType(), value));
    }
}
