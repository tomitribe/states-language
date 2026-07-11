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

import io.github.aglibs.validcheck.ValidCheck;
import jakarta.json.bind.annotation.JsonbProperty;
import lombok.Builder;
import lombok.Singular;

import java.util.List;

/**
 * A retry policy for a Task, Parallel or Map state.  When the state reports
 * an error whose name appears in {@code errorEquals}, the interpreter retries
 * the state at increasing intervals until {@code maxAttempts} is exhausted.
 *
 * The retry counters apply across all visits to this Retrier within a single
 * state execution and reset once the interpreter transitions to another state.
 *
 * Note that error name matching is exact and case-sensitive: catching
 * "java.lang.Exception" does not catch subclasses.  The reserved name
 * "States.ALL" matches any error and must appear alone in the last Retrier.
 *
 * @param errorEquals error names this Retrier matches; required, non-empty
 * @param intervalSeconds seconds before the first retry, default 1
 * @param maxAttempts maximum retries, default 3; zero — readable as
 *                    {@link MaxAttempts#NEVER} — is legal and means never retry
 * @param maxDelaySeconds upper bound in seconds on the computed retry interval
 * @param backoffRate multiplier applied to the interval on each attempt, default 2.0
 * @param jitterStrategy interpreter-defined jitter strategy name; AWS defines
 *                       {@link JitterStrategy#NONE} and {@link JitterStrategy#FULL}
 * @see <a href="https://states-language.net/spec.html#retrying-after-error">Retrying after error</a>
 */
@Builder(toBuilder = true, builderClassName = "Builder")
public record Retrier(@JsonbProperty("ErrorEquals") @Singular("error") List<String> errorEquals,
                      @JsonbProperty("IntervalSeconds") Integer intervalSeconds,
                      @JsonbProperty("MaxAttempts") Integer maxAttempts,
                      @JsonbProperty("MaxDelaySeconds") Integer maxDelaySeconds,
                      @JsonbProperty("BackoffRate") Double backoffRate,
                      @JsonbProperty("JitterStrategy") String jitterStrategy) {
    public Retrier {
        errorEquals = errorEquals == null || errorEquals.isEmpty() ? null : List.copyOf(errorEquals);
        ValidCheck.requireNotNull(errorEquals, "errorEquals");
    }

    /**
     * Named values for the "MaxAttempts" field.  The spec notes a value of
     * zero is legal, specifying that some error or errors should never be
     * retried — {@code maxAttempts(MaxAttempts.NEVER)} says so in code.
     */
    public interface MaxAttempts {

        int NEVER = 0;
    }

    /**
     * Named values for the "JitterStrategy" field.  The States Language
     * does not constrain the value — it is an interpreter-defined string —
     * but AWS Step Functions defines exactly these two.
     */
    public interface JitterStrategy {

        /**
         * The computed retry interval is used as is
         */
        String NONE = "NONE";

        /**
         * AWS replaces the computed retry interval with a random delay
         * between zero and that interval, spreading out retries from
         * simultaneous failures
         */
        String FULL = "FULL";
    }
}
