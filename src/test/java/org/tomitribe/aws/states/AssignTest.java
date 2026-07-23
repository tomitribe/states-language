/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tomitribe.aws.states;

import org.tomitribe.aws.states.test.JsonAsserts;
import jakarta.json.Json;
import jakarta.json.JsonValue;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTypeAdapter;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AssignTest {

    @Test
    public void valuesAndExpressions() throws Exception {
        final Assign assign = Assign.builder()
                .value("courier", "UQS")
                .value("attempts", 0)
                .value("rate", 2.5)
                .value("enabled", true)
                .value("coords", Json.createObjectBuilder().add("x", 1).build())
                .expression("product", "$states.input.product")
                .build();

        JsonAsserts.assertJson("""
                {
                  "courier": "UQS",
                  "attempts": 0,
                  "rate": 2.5,
                  "enabled": true,
                  "coords": { "x": 1 },
                  "product": "{% $states.input.product %}"
                }""", assign.variables().toString());
    }

    @Test
    public void typedGetters() {
        final Assign assign = Assign.builder()
                .value("courier", "UQS")
                .value("attempts", 3)
                .value("rate", 2.5)
                .value("enabled", true)
                .build();

        assertEquals("UQS", assign.getString("courier"));
        assertEquals(3, assign.getInt("attempts"));
        assertEquals(2.5, assign.getDouble("rate"));
        assertTrue(assign.getBoolean("enabled"));
        assertEquals(Set.of("courier", "attempts", "rate", "enabled"), assign.names());
    }

    @Test
    public void expressionsCanBeReadBack() {
        final Assign assign = Assign.builder()
                .expression("product", "$states.input.product")
                .value("courier", "UQS")
                .build();

        assertTrue(assign.isExpression("product"));
        assertFalse(assign.isExpression("courier"));
        assertEquals("$states.input.product", assign.getExpression("product"));

        final IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                () -> assign.getExpression("courier"));
        assertEquals("Assign member \"courier\" is not a JSONata expression: \"UQS\"", failure.getMessage());
    }

    @Test
    public void missingVariableNamesTheAlternatives() {
        final Assign assign = Assign.builder()
                .value("product", "tomcat")
                .value("version", "10.1.2-TT.1")
                .build();

        final IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                () -> assign.getString("produtc"));

        assertEquals("Assign has no member named \"produtc\".  "
                + "Members present: product, version", failure.getMessage());
    }

    @Test
    public void typeMismatchNamesTheActualType() {
        final Assign assign = Assign.builder()
                .value("attempts", 3)
                .build();

        final IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                () -> assign.getString("attempts"));

        assertEquals("Assign member \"attempts\" is not a string.  It is a NUMBER: 3", failure.getMessage());
    }

    /**
     * A literal that looks like an expression is almost certainly a
     * mistake; the interpreter would evaluate it
     */
    @Test
    public void valueRejectsExpressionStrings() {
        final IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                () -> Assign.builder().value("product", "{% $states.input.product %}"));

        assertEquals("Value for variable \"product\" looks like a JSONata expression: "
                + "\"{% $states.input.product %}\".  "
                + "Use expression(\"product\", \"$states.input.product\") instead", failure.getMessage());
    }

    @Test
    public void expressionRejectsDelimitedStrings() {
        final IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                () -> Assign.builder().expression("product", "{% $states.input.product %}"));

        assertEquals("Expression for variable \"product\" is already delimited: "
                + "\"{% $states.input.product %}\".  "
                + "Pass the expression without the {% %} delimiters", failure.getMessage());
    }

    @Test
    public void toBuilderRoundTrips() {
        final Assign original = Assign.builder()
                .value("courier", "UQS")
                .expression("product", "$states.input.product")
                .build();

        final Assign copy = original.toBuilder()
                .value("attempts", 1)
                .build();

        assertEquals("UQS", copy.getString("courier"));
        assertEquals("$states.input.product", copy.getExpression("product"));
        assertEquals(1, copy.getInt("attempts"));
        assertEquals(Set.of("courier", "product"), original.names());
    }

    /**
     * The behavior the states will rely on, verified through an owning
     * object.  Johnzon 2.1.0 honors the class-level @JsonbTypeAdapter on
     * Assign when serializing but ignores it when deserializing a record
     * field, attempting the record's canonical constructor instead — so
     * the owning field must carry the annotation itself, as Holder does
     * here and the states do in the model.
     */
    @Test
    public void fieldLevelAdapterAppliesBothDirections() throws Exception {
        final Holder holder = new Holder(Assign.builder()
                .value("courier", "UQS")
                .expression("product", "$states.input.product")
                .build());

        final String expected = """
                {
                  "Assign": {
                    "courier": "UQS",
                    "product": "{% $states.input.product %}"
                  }
                }""";

        try (Jsonb jsonb = JsonbBuilder.create()) {
            JsonAsserts.assertJson(expected, jsonb.toJson(holder));
        }

        try (Jsonb jsonb = JsonbBuilder.create()) {
            assertEquals(holder, jsonb.fromJson(expected, Holder.class));
        }
    }

    /**
     * The adapter runs on deserialization, so an owning object read from
     * a document with an illegal variable name fails on unmarshal
     */
    @Test
    public void classLevelAdapterValidatesOnRead() throws Exception {
        try (Jsonb jsonb = JsonbBuilder.create()) {
            final Exception failure = assertThrows(Exception.class,
                    () -> jsonb.fromJson("{\"Assign\":{\"states\":1}}", Holder.class));

            assertTrue(rootCause(failure).getMessage().contains(
                    "reserved by the States Language"), failure.toString());
        }
    }

    @Test
    public void nullAssignFieldIsOmitted() throws Exception {
        try (Jsonb jsonb = JsonbBuilder.create()) {
            JsonAsserts.assertJson("{}", jsonb.toJson(new Holder(null)));
        }
    }

    public record Holder(@JsonbProperty("Assign")
                         @JsonbTypeAdapter(Assign.Adapter.class) Assign assign) {
    }

    private Throwable rootCause(final Throwable throwable) {
        Throwable root = throwable;
        while (root.getCause() != null) root = root.getCause();
        return root;
    }

    @Test
    public void adapterRoundTrips() throws Exception {
        final Assign.Adapter adapter = new Assign.Adapter();

        final Assign assign = Assign.builder()
                .value("courier", "UQS")
                .expression("product", "$states.input.product")
                .build();

        final Assign read = adapter.adaptFromJson(adapter.adaptToJson(assign));
        assertEquals(assign, read);
    }

    /**
     * The adapter validates, so a bad document fails on unmarshal
     */
    @Test
    public void adapterValidates() {
        final Assign.Adapter adapter = new Assign.Adapter();

        assertThrows(IllegalArgumentException.class, () -> adapter.adaptFromJson(
                Json.createObjectBuilder().add("states", JsonValue.NULL).build()));
    }
}
