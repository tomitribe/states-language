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

import jakarta.json.JsonArray;
import jakarta.json.JsonValue;

/**
 * The value of a Map State's "Items" field: the array whose elements the
 * ItemProcessor runs once per item, called the Items Array.  The spec
 * allows a JSON array or a JSONata string that must produce one, so Items
 * takes exactly two forms: {@link ItemsExpression}, a single JSONata
 * expression producing the entire array, and {@link ItemsArray}, the array
 * given inline.
 *
 * Serializes as the delimited expression string or the plain JSON array.
 *
 * @see <a href="https://states-language.net/spec.html#selecting-items">Selecting Items</a>
 */
public sealed interface Items permits ItemsArray, ItemsExpression {

    JsonValue toJsonValue();

    static ItemsExpression expression(final String expression) {
        return new ItemsExpression(expression);
    }

    static ItemsArray.Builder builder() {
        return ItemsArray.builder();
    }

    /**
     * Serializes an Items as the delimited expression string or the plain
     * JSON array it represents, restoring the correct form on
     * deserialization
     */
    class Adapter extends UnionAdapter<Items, JsonArray> {

        public Adapter() {
            super("Items", "array", JsonArray.class,
                    ItemsArray::new, ItemsExpression::new, Items::toJsonValue);
        }
    }
}
