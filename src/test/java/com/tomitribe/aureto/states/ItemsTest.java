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

import com.tomitribe.aureto.states.test.JsonAsserts;
import jakarta.json.Json;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTypeAdapter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Items shares its mechanics with Arguments and Output through the same
 * hidden machinery; its structural form is a JSON array rather than an
 * object, so the array-side builder and the adapter's array arm are what
 * this test covers.
 */
class ItemsTest {

    @Test
    public void expressionForm() {
        final ItemsExpression shipped = Items.expression("$states.input.detail.shipped");

        assertEquals("$states.input.detail.shipped", shipped.expression());
        assertEquals("\"{% $states.input.detail.shipped %}\"", shipped.toJsonValue().toString());
    }

    /**
     * Elements mix literals and expressions, evaluated in place
     */
    @Test
    public void arrayForm() throws Exception {
        final ItemsArray items = Items.builder()
                .item(1)
                .expression("$two")
                .item("three")
                .item(true)
                .item(Json.createObjectBuilder().add("prod", "R31").build())
                .build();

        JsonAsserts.assertJson("""
                [ 1, "{% $two %}", "three", true, { "prod": "R31" } ]""",
                items.toJsonValue().toString());
    }

    @Test
    public void itemRejectsExpressionStrings() {
        final IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                () -> Items.builder().item("{% $two %}"));

        assertEquals("Item looks like a JSONata expression: \"{% $two %}\"."
                + "  Use expression(\"$two\") instead", failure.getMessage());
    }

    @Test
    public void toBuilderRoundTrips() {
        final ItemsArray original = Items.builder().item(1).build();
        final ItemsArray copy = original.toBuilder().item(2).build();

        assertEquals(2, copy.items().size());
        assertEquals(1, original.items().size());
    }

    /**
     * The adapter restores the correct form: a delimited string becomes an
     * ItemsExpression with the delimiters stripped, an array becomes an
     * ItemsArray, and anything else fails naming the field and its array
     * requirement
     */
    @Test
    public void adapterRestoresTheForm() throws Exception {
        final Items.Adapter adapter = new Items.Adapter();

        final ItemsExpression expression = Items.expression("$states.input");
        assertEquals(expression, adapter.adaptFromJson(adapter.adaptToJson(expression)));

        final ItemsArray items = Items.builder().item(1).item(2).build();
        assertEquals(items, adapter.adaptFromJson(adapter.adaptToJson(items)));

        final IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                () -> adapter.adaptFromJson(Json.createObjectBuilder().add("a", 1).build()));
        assertEquals("Items must be a JSON array or a JSONata expression string "
                + "delimited by {% %}.  Found OBJECT: {\"a\":1}", failure.getMessage());
    }

    @Test
    public void fieldLevelAdapterAppliesBothDirections() throws Exception {
        final Holder expression = new Holder(Items.expression("$states.input"));
        final Holder items = new Holder(Items.builder().item(1).expression("$two").build());

        try (Jsonb jsonb = JsonbBuilder.create()) {
            JsonAsserts.assertJson("{\"Items\": \"{% $states.input %}\"}", jsonb.toJson(expression));
            JsonAsserts.assertJson("{\"Items\": [1, \"{% $two %}\"]}", jsonb.toJson(items));
        }

        try (Jsonb jsonb = JsonbBuilder.create()) {
            assertEquals(expression, jsonb.fromJson("{\"Items\":\"{% $states.input %}\"}", Holder.class));
            assertEquals(items, jsonb.fromJson("{\"Items\":[1,\"{% $two %}\"]}", Holder.class));
        }
    }

    public record Holder(@JsonbProperty("Items")
                         @JsonbTypeAdapter(Items.Adapter.class) Items items) {
    }
}
