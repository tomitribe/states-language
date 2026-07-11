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
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArgumentsTest {

    @Test
    public void expressionForm() {
        final ArgumentsExpression envelope = Arguments.expression(
                "$merge([$states.input, {'step': $states.context.State.Name}])");

        assertEquals("$merge([$states.input, {'step': $states.context.State.Name}])",
                envelope.expression());
        assertEquals("\"{% $merge([$states.input, {'step': $states.context.State.Name}]) %}\"",
                envelope.toJsonValue().toString());
    }

    @Test
    public void expressionRejectsDelimitedStrings() {
        final IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                () -> Arguments.expression("{% $states.input %}"));

        assertEquals("Expression is already delimited: \"{% $states.input %}\".  "
                + "Pass the expression without the {% %} delimiters", failure.getMessage());
    }

    @Test
    public void objectForm() throws Exception {
        final ArgumentsObject object = Arguments.builder()
                .expression("step", "$states.context.State.Name")
                .expression("total", "$count($states.input)")
                .value("courier", "UQS")
                .value("attempts", 2)
                .value("rate", 2.5)
                .value("enabled", true)
                .build();

        JsonAsserts.assertJson("""
                {
                  "step": "{% $states.context.State.Name %}",
                  "total": "{% $count($states.input) %}",
                  "courier": "UQS",
                  "attempts": 2,
                  "rate": 2.5,
                  "enabled": true
                }""", object.toJsonValue().toString());
    }

    /**
     * Expressions may appear at any depth, so Arguments composes with
     * itself: a nested object becomes a nested object, a nested expression
     * becomes a delimited string evaluated in place
     */
    @Test
    public void nesting() throws Exception {
        final ArgumentsObject object = Arguments.builder()
                .value("classInfo", Arguments.builder()
                        .expression("teacher", "$class.teacher")
                        .build())
                .value("everything", Arguments.expression("$states.input"))
                .build();

        JsonAsserts.assertJson("""
                {
                  "classInfo": {
                    "teacher": "{% $class.teacher %}"
                  },
                  "everything": "{% $states.input %}"
                }""", object.toJsonValue().toString());
    }

    @Test
    public void valueRejectsExpressionStrings() {
        final IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                () -> Arguments.builder().value("step", "{% $states.context.State.Name %}"));

        assertEquals("Value for argument \"step\" looks like a JSONata expression: "
                + "\"{% $states.context.State.Name %}\".  "
                + "Use expression(\"step\", \"$states.context.State.Name\") instead", failure.getMessage());
    }

    @Test
    public void toBuilderRoundTrips() {
        final ArgumentsObject original = Arguments.builder()
                .value("courier", "UQS")
                .build();

        final ArgumentsObject copy = original.toBuilder()
                .value("attempts", 1)
                .build();

        assertEquals("UQS", copy.getString("courier"));
        assertEquals(1, copy.getInt("attempts"));
        assertEquals(1, original.values().size());
    }

    /**
     * The adapter restores the correct form: a delimited string becomes an
     * ArgumentsExpression with the delimiters stripped, an object becomes
     * an ArgumentsObject
     */
    @Test
    public void adapterRestoresTheForm() throws Exception {
        final Arguments.Adapter adapter = new Arguments.Adapter();

        final ArgumentsExpression expression = Arguments.expression("$states.input");
        assertEquals(expression, adapter.adaptFromJson(adapter.adaptToJson(expression)));

        final ArgumentsObject object = Arguments.builder().value("courier", "UQS").build();
        assertEquals(object, adapter.adaptFromJson(adapter.adaptToJson(object)));
    }

    /**
     * The spec allows any JSON text, but a form the model cannot represent
     * fails with a message naming what was found.  A bare string is almost
     * certainly an expression missing its delimiters.
     */
    @Test
    public void unmodeledFormsAreRejected() {
        final Arguments.Adapter adapter = new Arguments.Adapter();

        final IllegalArgumentException array = assertThrows(IllegalArgumentException.class,
                () -> adapter.adaptFromJson(Json.createArrayBuilder().add(1).build()));
        assertEquals("Arguments must be a JSON object or a JSONata expression string "
                + "delimited by {% %}.  Found ARRAY: [1]", array.getMessage());

        final IllegalArgumentException bare = assertThrows(IllegalArgumentException.class,
                () -> adapter.adaptFromJson(Json.createValue("$states.input")));
        assertTrue(bare.getMessage().contains("Found STRING"), bare.getMessage());
    }

    /**
     * Verified through an owning object because Johnzon 2.1.0 ignores
     * class-level adapters when deserializing record fields; the owning
     * field carries the annotation, as the states will
     */
    @Test
    public void fieldLevelAdapterAppliesBothDirections() throws Exception {
        final Holder expression = new Holder(Arguments.expression("$states.input"));
        final Holder object = new Holder(Arguments.builder().value("courier", "UQS").build());

        try (Jsonb jsonb = JsonbBuilder.create()) {
            JsonAsserts.assertJson("{\"Arguments\": \"{% $states.input %}\"}", jsonb.toJson(expression));
            JsonAsserts.assertJson("{\"Arguments\": {\"courier\": \"UQS\"}}", jsonb.toJson(object));
        }

        try (Jsonb jsonb = JsonbBuilder.create()) {
            assertEquals(expression, jsonb.fromJson("{\"Arguments\":\"{% $states.input %}\"}", Holder.class));
            assertEquals(object, jsonb.fromJson("{\"Arguments\":{\"courier\":\"UQS\"}}", Holder.class));
        }
    }

    public record Holder(@JsonbProperty("Arguments")
                         @JsonbTypeAdapter(Arguments.Adapter.class) Arguments arguments) {
    }
}
