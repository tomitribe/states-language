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

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * State names must be at most 80 Unicode characters, counted as code
 * points.  The rule applies anywhere a "States" field appears: the state
 * machine, a Parallel branch, and a Map ItemProcessor.
 */
class StateNameTest {

    private static final String EIGHTY = "x".repeat(80);
    private static final String EIGHTY_ONE = "x".repeat(81);

    @Test
    public void eightyCharactersIsLegal() {
        StateMachine.builder()
                .startAt(EIGHTY)
                .states(Map.of(EIGHTY, pass()))
                .build();
    }

    @Test
    public void eightyOneCharactersIsRejected() {
        final IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                () -> StateMachine.builder()
                        .startAt("Start")
                        .states(Map.of(EIGHTY_ONE, pass()))
                        .build());

        assertEquals("State name is 81 characters long, exceeding the States Language "
                + "maximum of 80: \"" + EIGHTY_ONE + "\"", failure.getMessage());
    }

    /**
     * The spec counts Unicode characters, so 80 emoji is a legal name even
     * though it is 160 Java chars
     */
    @Test
    public void unicodeCharactersAreCountedAsCodePoints() {
        final String emoji = "🙂".repeat(80);
        assertEquals(160, emoji.length());

        StateMachine.builder()
                .startAt(emoji)
                .states(Map.of(emoji, pass()))
                .build();
    }

    @Test
    public void branchNamesAreChecked() {
        final IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                () -> Branch.builder()
                        .startAt("Start")
                        .states(Map.of(EIGHTY_ONE, pass()))
                        .build());

        assertTrue(failure.getMessage().contains(EIGHTY_ONE));
    }

    @Test
    public void itemProcessorNamesAreChecked() {
        final IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                () -> ItemProcessor.builder()
                        .startAt("Start")
                        .states(Map.of(EIGHTY_ONE, pass()))
                        .build());

        assertTrue(failure.getMessage().contains(EIGHTY_ONE));
    }

    private PassState pass() {
        return PassState.builder().end(true).build();
    }
}
