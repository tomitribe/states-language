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

import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

import java.util.Set;

/**
 * Typed, expression-aware reads over the model's JSON object types —
 * Assign, ArgumentsObject, OutputObject — whose member values are each a
 * JSON literal or a JSONata expression.  Implementations supply
 * {@link #toJsonObject()}; everything else is derived.
 *
 * Misses and type mismatches fail with messages naming the owning type,
 * the members that do exist, and the actual JSON type found.
 */
public interface Values {

    JsonObject toJsonObject();

    default Set<String> names() {
        return toJsonObject().keySet();
    }

    /**
     * The raw value of the named member, expression or literal
     */
    default JsonValue get(final String name) {
        final JsonValue value = toJsonObject().get(name);
        if (value == null) {
            throw new IllegalArgumentException(String.format(
                    "%s has no member named \"%s\".  Members present: %s",
                    getClass().getSimpleName(), name, String.join(", ", names())));
        }
        return value;
    }

    default String getString(final String name) {
        final JsonValue value = get(name);
        if (!(value instanceof JsonString string)) throw mismatch(name, value, "string");
        return string.getString();
    }

    default int getInt(final String name) {
        final JsonValue value = get(name);
        if (!(value instanceof JsonNumber number)) throw mismatch(name, value, "number");
        return number.intValueExact();
    }

    default double getDouble(final String name) {
        final JsonValue value = get(name);
        if (!(value instanceof JsonNumber number)) throw mismatch(name, value, "number");
        return number.doubleValue();
    }

    default boolean getBoolean(final String name) {
        final JsonValue value = get(name);
        if (value == JsonValue.TRUE) return true;
        if (value == JsonValue.FALSE) return false;
        throw mismatch(name, value, "boolean");
    }

    /**
     * True if the named member's value is a JSONata expression, a string
     * delimited by "{%" and "%}"
     */
    default boolean isExpression(final String name) {
        return get(name) instanceof JsonString string && Expressions.isDelimited(string.getString());
    }

    /**
     * The JSONata expression the named member holds, without the "{%" and
     * "%}" delimiters
     */
    default String getExpression(final String name) {
        final JsonValue value = get(name);
        if (!(value instanceof JsonString string) || !Expressions.isDelimited(string.getString())) {
            throw new IllegalArgumentException(String.format(
                    "%s member \"%s\" is not a JSONata expression: %s",
                    getClass().getSimpleName(), name, value));
        }
        return Expressions.strip(string.getString());
    }

    private IllegalArgumentException mismatch(final String name, final JsonValue value, final String expected) {
        return new IllegalArgumentException(String.format(
                "%s member \"%s\" is not a %s.  It is a %s: %s",
                getClass().getSimpleName(), name, expected, value.getValueType(), value));
    }
}
