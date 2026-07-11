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
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The shared builder vocabulary for the model's JSON object types — Assign,
 * ArgumentsObject, OutputObject — whose member values are each a JSON
 * literal or a JSONata expression.  The {@code value} methods add literals,
 * guarding against strings that look like expressions; the
 * {@code expression} method adds expressions, supplying the "{%" and "%}"
 * delimiters so callers never write them.
 *
 * Deliberately not public: each owning type exposes its own Builder, which
 * supplies the member-name policy through {@link #checkName} — Assign
 * enforces the variable naming rules, the others accept any name.
 *
 * @param <B> the concrete builder, so the fluent methods return the subtype
 * @param <T> the type the builder builds
 */
abstract class ObjectBuilder<B extends ObjectBuilder<B, T>, T> {

    private final Map<String, JsonValue> members = new LinkedHashMap<>();

    /**
     * What a member is called in this builder's error messages, for
     * example "variable" or "argument"
     */
    private final String noun;

    ObjectBuilder(final String noun) {
        this.noun = noun;
    }

    public B value(final String name, final String value) {
        ValidCheck.requireNotNull(value, "value");
        if (Expressions.isDelimited(value)) {
            throw new IllegalArgumentException(String.format(
                    "Value for %s \"%s\" looks like a JSONata expression: \"%s\"."
                            + "  Use expression(\"%s\", \"%s\") instead",
                    noun, name, value, name, Expressions.strip(value)));
        }
        return value(name, Json.createValue(value));
    }

    public B value(final String name, final int value) {
        return value(name, Json.createValue(value));
    }

    public B value(final String name, final double value) {
        return value(name, Json.createValue(value));
    }

    public B value(final String name, final boolean value) {
        return value(name, value ? JsonValue.TRUE : JsonValue.FALSE);
    }

    public B value(final String name, final JsonValue value) {
        ValidCheck.requireNotNull(value, "value");
        members.put(checkName(name), value);
        return self();
    }

    /**
     * Sets the named member to a JSONata expression, wrapping it in the
     * "{%" and "%}" delimiters.  Pass the bare expression, for example
     * {@code expression("total", "$count($states.input)")}
     */
    public B expression(final String name, final String expression) {
        ValidCheck.requireNotNull(expression, "expression");
        if (Expressions.isDelimited(expression)) {
            throw new IllegalArgumentException(String.format(
                    "Expression for %s \"%s\" is already delimited: \"%s\"."
                            + "  Pass the expression without the {%% %%} delimiters",
                    noun, name, expression));
        }
        return value(name, Json.createValue(Expressions.delimit(expression)));
    }

    public abstract T build();

    /**
     * The member name rules of the owning type; the default accepts any
     * non-null name
     */
    protected String checkName(final String name) {
        ValidCheck.requireNotNull(name, "name");
        return name;
    }

    protected abstract B self();

    final B putAll(final JsonObject members) {
        this.members.putAll(members);
        return self();
    }

    final JsonObject toJsonObject() {
        final var json = Json.createObjectBuilder();
        members.forEach(json::add);
        return json.build();
    }
}
