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

/**
 * The States Language MUST rules that are decidable from a single object's
 * own fields, enforced from the record constructors so both built and
 * unmarshalled machines are checked.  Rules that need the whole machine —
 * "Next" targets exist, "StartAt" matches a state — belong to a future
 * validator, not here.
 *
 * Deliberately not public: callers exercise these rules by constructing
 * model objects, not by calling validation methods.
 */
final class Rules {

    private Rules() {
    }

    /**
     * Task, Parallel, Map, Pass, and Wait States require exactly one of a
     * "Next" transition or {@code "End": true}
     */
    static void requireTransition(final Class<?> state, final String next, final Boolean end) {
        final boolean terminal = Boolean.TRUE.equals(end);
        if (next == null && !terminal) {
            throw new IllegalArgumentException(String.format(
                    "%s must have a \"Next\" field or \"End\": true", state.getSimpleName()));
        }
        if (next != null && terminal) {
            throw new IllegalArgumentException(String.format(
                    "%s must not have both a \"Next\" field and \"End\": true", state.getSimpleName()));
        }
    }

    static Integer requirePositive(final String field, final Integer value) {
        if (value != null && value < 1) {
            throw new IllegalArgumentException(String.format(
                    "\"%s\" must be a positive integer: %s", field, value));
        }
        return value;
    }

    static Integer requireNonNegative(final String field, final Integer value) {
        if (value != null && value < 0) {
            throw new IllegalArgumentException(String.format(
                    "\"%s\" must be a non-negative integer: %s", field, value));
        }
        return value;
    }

    static Double requirePercentage(final String field, final Double value) {
        if (value != null && (value < 0 || value > 100)) {
            throw new IllegalArgumentException(String.format(
                    "\"%s\" must be a number between zero and 100: %s", field, value));
        }
        return value;
    }

    static Double requireMinimum(final String field, final Double value, final double minimum) {
        if (value != null && value < minimum) {
            throw new IllegalArgumentException(String.format(
                    "\"%s\" must be greater than or equal to %s: %s", field, minimum, value));
        }
        return value;
    }
}
