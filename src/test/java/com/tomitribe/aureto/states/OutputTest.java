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
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTypeAdapter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Output shares its mechanics with Arguments through the same hidden
 * machinery, so this test covers the type's identity — its own forms, its
 * own adapter, its own error messages — rather than re-proving the shared
 * vocabulary that ArgumentsTest exercises.
 */
class OutputTest {

    @Test
    public void expressionForm() {
        final OutputExpression merge = Output.expression(
                "$merge([$states.input, {'input': $states.result}])");

        assertEquals("$merge([$states.input, {'input': $states.result}])", merge.expression());
        assertEquals("\"{% $merge([$states.input, {'input': $states.result}]) %}\"",
                merge.toJsonValue().toString());
    }

    @Test
    public void objectForm() throws Exception {
        final OutputObject object = Output.builder()
                .expression("customer", "$states.input.customer")
                .expression("resultStatus", "$states.result.status")
                .value("processed", true)
                .build();

        JsonAsserts.assertJson("""
                {
                  "customer": "{% $states.input.customer %}",
                  "resultStatus": "{% $states.result.status %}",
                  "processed": true
                }""", object.toJsonValue().toString());
    }

    @Test
    public void messagesNameTheField() {
        final IllegalArgumentException value = assertThrows(IllegalArgumentException.class,
                () -> Output.builder().value("customer", "{% $states.input.customer %}"));
        assertEquals("Value for field \"customer\" looks like a JSONata expression: "
                + "\"{% $states.input.customer %}\".  "
                + "Use expression(\"customer\", \"$states.input.customer\") instead", value.getMessage());

        final IllegalArgumentException union = assertThrows(IllegalArgumentException.class,
                () -> new Output.Adapter().adaptFromJson(jakarta.json.Json.createValue(42)));
        assertEquals("Output must be a JSON object or a JSONata expression string "
                + "delimited by {% %}.  Found NUMBER: 42", union.getMessage());
    }

    @Test
    public void fieldLevelAdapterAppliesBothDirections() throws Exception {
        final Holder expression = new Holder(Output.expression("$states.result"));
        final Holder object = new Holder(Output.builder().value("processed", true).build());

        try (Jsonb jsonb = JsonbBuilder.create()) {
            JsonAsserts.assertJson("{\"Output\": \"{% $states.result %}\"}", jsonb.toJson(expression));
            JsonAsserts.assertJson("{\"Output\": {\"processed\": true}}", jsonb.toJson(object));
        }

        try (Jsonb jsonb = JsonbBuilder.create()) {
            assertEquals(expression, jsonb.fromJson("{\"Output\":\"{% $states.result %}\"}", Holder.class));
            assertEquals(object, jsonb.fromJson("{\"Output\":{\"processed\":true}}", Holder.class));
        }
    }

    public record Holder(@JsonbProperty("Output")
                         @JsonbTypeAdapter(Output.Adapter.class) Output output) {
    }
}
