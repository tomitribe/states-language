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
 * The value of a Map State's "ItemSelector" field: overrides each single
 * element of the Items Array to produce the selected item passed to the
 * ItemProcessor.  The spec allows a JSON text or a JSONata string that
 * evaluates to one, so ItemSelector takes exactly two forms:
 * {@link ItemSelectorExpression}, a single JSONata expression producing
 * the entire value, and {@link ItemSelectorObject}, a JSON object of named
 * fields.
 *
 * Serializes as the delimited expression string or the plain JSON object.
 *
 * @see <a href="https://states-language.net/spec.html#selecting-items">Selecting Items</a>
 */
public sealed interface ItemSelector permits ItemSelectorObject, ItemSelectorExpression {

    JsonValue toJsonValue();

    static ItemSelectorExpression expression(final String expression) {
        return new ItemSelectorExpression(expression);
    }

    static ItemSelectorObject.Builder builder() {
        return ItemSelectorObject.builder();
    }

    /**
     * Serializes an ItemSelector as the delimited expression string or the
     * plain JSON object it represents, restoring the correct form on
     * deserialization
     */
    class Adapter extends UnionAdapter<ItemSelector, JsonObject> {

        public Adapter() {
            super("ItemSelector", "object", JsonObject.class,
                    ItemSelectorObject::new, ItemSelectorExpression::new, ItemSelector::toJsonValue);
        }
    }
}
