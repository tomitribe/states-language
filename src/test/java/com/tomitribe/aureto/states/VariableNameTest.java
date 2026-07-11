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

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Variable names must be at most 80 Unicode characters, must not be the
 * reserved name "states", and must be Unicode identifiers: an ID_Start
 * character followed by ID_Continue characters.
 *
 * The rules are enforced by the Assign class itself, so an invalid
 * assignment is unrepresentable in the model — the only paths into a state
 * are Assign's builder and its Adapter, and both check.
 */
class VariableNameTest {

    @Test
    public void validNamesAreAccepted() {
        Assign.builder()
                .value("product", "tomcat")
                .value("version2", "10.1.2-TT.1")
                .value("μεταβλητή", "unicode is fine")
                .build();
    }

    @Test
    public void eightyOneCharactersIsRejected() {
        final String eightyOne = "x".repeat(81);

        final IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                () -> Assign.builder().value(eightyOne, "too long"));

        assertEquals("Variable name is 81 characters long, exceeding the States Language "
                + "maximum of 80: \"" + eightyOne + "\"", failure.getMessage());
    }

    @Test
    public void reservedStatesVariableIsRejected() {
        final IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                () -> Assign.builder().value("states", "reserved"));

        assertEquals("Variable name \"states\" is reserved by the States Language "
                + "and cannot be assigned", failure.getMessage());
    }

    /**
     * The first character must be a Unicode ID_Start character, which
     * excludes digits and, per UAX #31, the underscore
     */
    @Test
    public void invalidIdentifiersAreRejected() {
        for (final String name : new String[]{"9lives", "_hidden", "has space", "has-dash", ""}) {
            final IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                    () -> Assign.builder().value(name, "value"),
                    String.format("Expected \"%s\" to be rejected", name));

            assertTrue(failure.getMessage().contains("not a valid Unicode identifier"), name);
        }
    }

    /**
     * A state machine document assigning an illegal variable fails on
     * unmarshal, through the Assign adapter
     */
    @Test
    public void deserializedDocumentsAreChecked() throws Exception {
        final String document = """
                {
                  "StartAt": "A",
                  "States": {
                    "A": {
                      "Type": "Pass",
                      "Assign": { "states": 1 },
                      "End": true
                    }
                  }
                }""";

        try (Jsonb jsonb = JsonbBuilder.create()) {
            final Exception failure = assertThrows(Exception.class,
                    () -> jsonb.fromJson(document, StateMachine.class));

            assertTrue(rootCause(failure).getMessage().contains("reserved by the States Language"),
                    failure.toString());
        }
    }

    private Throwable rootCause(final Throwable throwable) {
        Throwable root = throwable;
        while (root.getCause() != null) root = root.getCause();
        return root;
    }
}
