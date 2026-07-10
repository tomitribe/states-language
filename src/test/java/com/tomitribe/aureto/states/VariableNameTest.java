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

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Variable names, the keys of any "Assign" object, must be at most 80
 * Unicode characters, must not be the reserved name "states", and must be
 * Unicode identifiers: an ID_Start character followed by ID_Continue
 * characters.
 */
class VariableNameTest {

    @Test
    public void validNamesAreAccepted() {
        PassState.builder()
                .assign(Json.createObjectBuilder()
                        .add("product", "tomcat")
                        .add("version2", "10.1.2-TT.1")
                        .add("μεταβλητή", "unicode is fine")
                        .build())
                .end(true)
                .build();
    }

    @Test
    public void eightyOneCharactersIsRejected() {
        final String eightyOne = "x".repeat(81);

        final IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                () -> PassState.builder().assign(assign(eightyOne)).end(true).build());

        assertEquals("Variable name is 81 characters long, exceeding the States Language "
                + "maximum of 80: \"" + eightyOne + "\"", failure.getMessage());
    }

    @Test
    public void reservedStatesVariableIsRejected() {
        final IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                () -> PassState.builder().assign(assign("states")).end(true).build());

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
                    () -> PassState.builder().assign(assign(name)).end(true).build(),
                    String.format("Expected \"%s\" to be rejected", name));

            assertTrue(failure.getMessage().contains("not a valid Unicode identifier"), name);
        }
    }

    /**
     * Assign appears on six states plus Catcher and ChoiceRule; all of
     * them check
     */
    @Test
    public void allAssignHoldersAreChecked() {
        final JsonObject reserved = assign("states");

        assertThrows(IllegalArgumentException.class, () -> TaskState.builder()
                .resource("arn:test").assign(reserved).end(true).build());
        assertThrows(IllegalArgumentException.class, () -> PassState.builder()
                .assign(reserved).end(true).build());
        assertThrows(IllegalArgumentException.class, () -> WaitState.builder()
                .seconds(1).assign(reserved).end(true).build());
        assertThrows(IllegalArgumentException.class, () -> ChoiceState.builder()
                .choice(rule()).assign(reserved).build());
        assertThrows(IllegalArgumentException.class, () -> ParallelState.builder()
                .branch(branch()).assign(reserved).end(true).build());
        assertThrows(IllegalArgumentException.class, () -> MapState.builder()
                .itemProcessor(processor()).assign(reserved).end(true).build());
        assertThrows(IllegalArgumentException.class, () -> Catcher.builder()
                .error("States.ALL").next("A").assign(reserved).build());
        assertThrows(IllegalArgumentException.class, () -> ChoiceRule.builder()
                .condition(JsonValue.TRUE).next("A").assign(reserved).build());
    }

    private JsonObject assign(final String name) {
        return Json.createObjectBuilder().add(name, "value").build();
    }

    private ChoiceRule rule() {
        return ChoiceRule.builder().condition(JsonValue.TRUE).next("A").build();
    }

    private Branch branch() {
        return Branch.builder().startAt("A").states(Map.of()).build();
    }

    private ItemProcessor processor() {
        return ItemProcessor.builder().startAt("A").states(Map.of()).build();
    }
}
