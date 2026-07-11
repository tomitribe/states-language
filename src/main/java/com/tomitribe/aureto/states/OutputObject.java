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
 * The object form of "Output": a JSON object whose field values are JSON
 * literals or JSONata expressions.  Field names are the state's own output
 * contract, so unlike Assign no naming rules apply.
 *
 * @param values the output fields, name to value; expressions are held in
 *               their delimited string form
 * @see <a href="https://states-language.net/spec.html#arguments-and-output">Using Arguments and Output</a>
 */
public record OutputObject(JsonObject values) implements Output, Values {

    public OutputObject {
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

    public static class Builder extends ObjectBuilder<Builder, OutputObject> {

        Builder() {
            super("field");
        }

        /**
         * Nests another Output as the named field's value: an object
         * becomes a nested object, an expression becomes a delimited
         * string the interpreter evaluates in place
         */
        public Builder value(final String name, final Output value) {
            ValidCheck.requireNotNull(value, "value");
            return value(name, value.toJsonValue());
        }

        @Override
        public OutputObject build() {
            return new OutputObject(toJsonObject());
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
