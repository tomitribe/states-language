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

import java.util.List;

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

    static void requireStatesAllPlacement(final List<Retrier> retry, final List<Catcher> catchers) {
        if (retry != null) {
            requireStatesAllPlacement("Retrier", "Retry",
                    retry.stream().map(Retrier::errorEquals).toList());
        }
        if (catchers != null) {
            requireStatesAllPlacement("Catcher", "Catch",
                    catchers.stream().map(Catcher::errorEquals).toList());
        }
    }

    /**
     * "States.ALL" must appear alone in its "ErrorEquals" array and only
     * in the last Retrier or Catcher of the list
     */
    private static void requireStatesAllPlacement(final String entryNoun, final String field,
                                          final List<List<String>> errorEquals) {
        if (errorEquals == null) return;
        for (int i = 0; i < errorEquals.size(); i++) {
            final List<String> errors = errorEquals.get(i);
            if (!errors.contains(Errors.States.ALL)) continue;

            if (errors.size() > 1) {
                throw new IllegalArgumentException(String.format(
                        "\"States.ALL\" must appear alone in \"ErrorEquals\": %s", errors));
            }
            if (i != errorEquals.size() - 1) {
                throw new IllegalArgumentException(String.format(
                        "The \"States.ALL\" %s must be the last entry in \"%s\";"
                                + " it is entry %s of %s",
                        entryNoun, field, i + 1, errorEquals.size()));
            }
        }
    }
}
