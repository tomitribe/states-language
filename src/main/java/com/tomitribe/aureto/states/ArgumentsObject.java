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
import jakarta.json.JsonValue;

/**
 * The object form of "Arguments": a JSON object of named arguments, each a
 * JSON literal or a JSONata expression.  Argument names are the task's own
 * contract, so unlike Assign no naming rules apply.
 *
 * Expressions may appear at any depth, so the builder composes: a nested
 * Arguments becomes a nested object or an expression evaluated in place.
 *
 * @param values the arguments, name to value; expressions are held in
 *               their delimited string form
 * @see <a href="https://states-language.net/spec.html#arguments-and-output">Using Arguments and Output</a>
 */
public record ArgumentsObject(JsonObject values) implements Arguments, Values {

    public ArgumentsObject {
        ValidCheck.requireNotNull(values, "values");
    }

    @Override
    public JsonValue toJsonValue() {
        return values;
    }

    @Override
    public JsonObject toJsonObject() {
        return values;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder().putAll(values);
    }

    public static class Builder extends ObjectBuilder<Builder, ArgumentsObject> {

        Builder() {
            super("argument");
        }

        /**
         * Nests another Arguments as the named argument's value: an object
         * becomes a nested object, an expression becomes a delimited
         * string the interpreter evaluates in place
         */
        public Builder value(final String name, final Arguments value) {
            ValidCheck.requireNotNull(value, "value");
            return value(name, value.toJsonValue());
        }

        @Override
        public ArgumentsObject build() {
            return new ArgumentsObject(toJsonObject());
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
