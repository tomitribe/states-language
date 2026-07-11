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
 * The object form of "BatchInput": a JSON object of named fields, each a
 * JSON literal or a JSONata expression, merged as fixed input into each
 * batch an ItemBatcher creates.
 *
 * @param values the batch input fields, name to value; expressions are
 *               held in their delimited string form
 * @see <a href="https://states-language.net/spec.html#batching-items">Batching Items</a>
 */
public record BatchInputObject(JsonObject values) implements BatchInput, Values {

    public BatchInputObject {
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

    public static class Builder extends ObjectBuilder<Builder, BatchInputObject> {

        Builder() {
            super("field");
        }

        /**
         * Nests another BatchInput as the named field's value: an object
         * becomes a nested object, an expression becomes a delimited
         * string the interpreter evaluates in place
         */
        public Builder value(final String name, final BatchInput value) {
            ValidCheck.requireNotNull(value, "value");
            return value(name, value.toJsonValue());
        }

        @Override
        public BatchInputObject build() {
            return new BatchInputObject(toJsonObject());
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
