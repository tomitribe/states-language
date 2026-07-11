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
import jakarta.json.JsonValue;
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
        assertEquals("Variable \"courier\" is not a JSONata expression: \"UQS\"", failure.getMessage());
    }

    @Test
    public void missingVariableNamesTheAlternatives() {
        final Assign assign = Assign.builder()
                .value("product", "tomcat")
                .value("version", "10.1.2-TT.1")
                .build();

        final IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                () -> assign.getString("produtc"));

        assertEquals("No variable named \"produtc\" is assigned.  "
                + "Assigned variables are: product, version", failure.getMessage());
    }

    @Test
    public void typeMismatchNamesTheActualType() {
        final Assign assign = Assign.builder()
                .value("attempts", 3)
                .build();

        final IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                () -> assign.getString("attempts"));

        assertEquals("Variable \"attempts\" is not a string.  It is a NUMBER: 3", failure.getMessage());
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
    public void variableNamesAreValidated() {
        assertThrows(IllegalArgumentException.class,
                () -> Assign.builder().value("states", "reserved"));
        assertThrows(IllegalArgumentException.class,
                () -> Assign.builder().value("9lives", "starts with a digit"));
        assertThrows(IllegalArgumentException.class,
                () -> Assign.builder().value("x".repeat(81), "too long"));
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
