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
 * The object form of "ItemSelector": a JSON object whose field values are
 * JSON literals or JSONata expressions, overriding each element of the
 * Items Array.  Expressions typically reference the item being processed
 * through {@code $states.context.Map.Item.Value} and
 * {@code $states.context.Map.Item.Index}.
 *
 * @param values the selected item's fields, name to value; expressions are
 *               held in their delimited string form
 * @see <a href="https://states-language.net/spec.html#selecting-items">Selecting Items</a>
 */
public record ItemSelectorObject(JsonObject values) implements ItemSelector, Values {

    public ItemSelectorObject {
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

    public static class Builder extends ObjectBuilder<Builder, ItemSelectorObject> {

        Builder() {
            super("field");
        }

        /**
         * Nests another ItemSelector as the named field's value: an object
         * becomes a nested object, an expression becomes a delimited
         * string the interpreter evaluates in place
         */
        public Builder value(final String name, final ItemSelector value) {
            ValidCheck.requireNotNull(value, "value");
            return value(name, value.toJsonValue());
        }

        @Override
        public ItemSelectorObject build() {
            return new ItemSelectorObject(toJsonObject());
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
