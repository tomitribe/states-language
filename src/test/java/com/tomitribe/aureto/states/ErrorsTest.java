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

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErrorsTest {

    /**
     * The two families carry their prefixes and no name repeats — the
     * constants exist to make ErrorEquals typos unrepresentable, so the
     * constants themselves must be typo-proof
     */
    @Test
    public void prefixesAndUniqueness() throws Exception {
        final List<String> states = values(Errors.States.class);
        final List<String> http = values(Errors.States.Http.class);
        final List<String> lambda = values(Errors.Lambda.class);
        final List<String> sandbox = values(Errors.Sandbox.class);

        states.forEach(value -> assertTrue(value.startsWith("States."), value));
        http.forEach(value -> assertTrue(value.startsWith("States.Http."), value));
        lambda.forEach(value -> assertTrue(value.startsWith("Lambda."), value));
        sandbox.forEach(value -> assertTrue(value.startsWith("Sandbox."), value));

        assertEquals(16, states.size());
        assertEquals(10, http.size());
        assertEquals(47, lambda.size());
        assertEquals(1, sandbox.size());

        final List<String> all = Stream.of(states, http, lambda, sandbox).flatMap(List::stream).toList();
        assertEquals(all.size(), all.stream().distinct().count(), "duplicate error name");
    }

    @Test
    public void spotChecks() {
        assertEquals("States.ALL", Errors.States.ALL);
        assertEquals("States.Timeout", Errors.States.TIMEOUT);
        assertEquals("States.ExceedToleratedFailureThreshold", Errors.States.EXCEED_TOLERATED_FAILURE_THRESHOLD);
        assertEquals("Lambda.TooManyRequestsException", Errors.Lambda.TOO_MANY_REQUESTS_EXCEPTION);
        assertEquals("Lambda.EC2ThrottledException", Errors.Lambda.EC2_THROTTLED_EXCEPTION);
        assertEquals("Lambda.SnapStartTimeoutException", Errors.Lambda.SNAP_START_TIMEOUT_EXCEPTION);
        assertEquals("Sandbox.Timedout", Errors.Sandbox.TIMEDOUT);
        assertEquals("States.Http.Socket", Errors.States.Http.SOCKET);
        assertEquals("States.Http.StatusCode.429", Errors.States.Http.STATUS_CODE_429);
        assertEquals("States.Http.StatusCode.418", Errors.States.Http.statusCode(418));
    }

    private List<String> values(final Class<?> constants) throws Exception {
        final List<String> values = Arrays.stream(constants.getFields())
                .filter(field -> field.getType() == String.class)
                .map(this::value)
                .toList();
        assertTrue(values.size() > 0, "no constants found in " + constants.getName());
        return values;
    }

    private String value(final Field field) {
        try {
            return (String) field.get(null);
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException(field.getName(), e);
        }
    }
}
