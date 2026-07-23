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
