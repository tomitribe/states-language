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
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.bind.adapter.JsonbAdapter;
import jakarta.json.bind.annotation.JsonbTypeAdapter;

import java.util.Set;

/**
 * The value of an "Assign" field: variable names mapped to the values they
 * will hold in the next state.  Each value is either a JSON literal or a
 * JSONata expression, a string delimited by "{%" and "%}".  This class owns
 * that duality: the builder's {@code value} methods add literals and its
 * {@code expression} method adds expressions, supplying the delimiters so
 * callers never write them; the {@code isExpression} and
 * {@code getExpression} methods read them back out.
 *
 * Variable names are validated as they are added: at most 80 Unicode
 * characters, not the reserved name "states", and a valid Unicode
 * identifier.
 *
 * Serializes as the plain JSON object, for example
 * <pre>
 * "Assign": {
 *   "product": "{% $states.input.product %}",
 *   "attempts": 0
 * }
 * </pre>
 *
 * @param variables the assigned variables, name to value
 * @see <a href="https://states-language.net/spec.html#assigning-variables">Assigning State Machine Variables</a>
 * @see <a href="https://states-language.net/spec.html#expressions">JSONata Expressions</a>
 */
@JsonbTypeAdapter(Assign.Adapter.class)
public record Assign(JsonObject variables) {

    public Assign {
        ValidCheck.requireNotNull(variables, "variables");
        variables.keySet().forEach(Names::requireValidVariableName);
    }

    public Set<String> names() {
        return variables.keySet();
    }

    /**
     * The raw value of the named variable, expression or literal
     */
    public JsonValue get(final String name) {
        final JsonValue value = variables.get(name);
        if (value == null) {
            throw new IllegalArgumentException(String.format(
                    "No variable named \"%s\" is assigned.  Assigned variables are: %s",
                    name, String.join(", ", names())));
        }
        return value;
    }

    public String getString(final String name) {
        final JsonValue value = get(name);
        if (!(value instanceof JsonString string)) throw mismatch(name, value, "string");
        return string.getString();
    }

    public int getInt(final String name) {
        final JsonValue value = get(name);
        if (!(value instanceof JsonNumber number)) throw mismatch(name, value, "number");
        return number.intValueExact();
    }

    public double getDouble(final String name) {
        final JsonValue value = get(name);
        if (!(value instanceof JsonNumber number)) throw mismatch(name, value, "number");
        return number.doubleValue();
    }

    public boolean getBoolean(final String name) {
        final JsonValue value = get(name);
        if (value == JsonValue.TRUE) return true;
        if (value == JsonValue.FALSE) return false;
        throw mismatch(name, value, "boolean");
    }

    /**
     * True if the named variable's value is a JSONata expression, a string
     * delimited by "{%" and "%}"
     */
    public boolean isExpression(final String name) {
        return get(name) instanceof JsonString string && Expressions.isDelimited(string.getString());
    }

    /**
     * The JSONata expression assigned to the named variable, without the
     * "{%" and "%}" delimiters
     */
    public String getExpression(final String name) {
        final JsonValue value = get(name);
        if (!(value instanceof JsonString string) || !Expressions.isDelimited(string.getString())) {
            throw new IllegalArgumentException(String.format(
                    "Variable \"%s\" is not a JSONata expression: %s", name, value));
        }
        return Expressions.strip(string.getString());
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder().putAll(variables);
    }

    private IllegalArgumentException mismatch(final String name, final JsonValue value, final String expected) {
        return new IllegalArgumentException(String.format(
                "Variable \"%s\" is not a %s.  It is a %s: %s",
                name, expected, value.getValueType(), value));
    }

    public static class Builder extends ObjectBuilder<Builder, Assign> {

        Builder() {
            super("variable");
        }

        @Override
        public Assign build() {
            return new Assign(toJsonObject());
        }

        @Override
        protected String checkName(final String name) {
            return Names.requireValidVariableName(name);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    /**
     * Serializes an Assign as the plain JSON object it wraps
     */
    public static class Adapter implements JsonbAdapter<Assign, JsonObject> {

        @Override
        public JsonObject adaptToJson(final Assign assign) {
            return assign.variables();
        }

        @Override
        public Assign adaptFromJson(final JsonObject variables) {
            return new Assign(variables);
        }
    }
}
