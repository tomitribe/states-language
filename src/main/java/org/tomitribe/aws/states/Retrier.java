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
 * @param errorEquals error names this Retrier matches; required, non-empty;
 *                    the predefined names are constants on {@link Errors}
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
        Rules.requirePositive("IntervalSeconds", intervalSeconds);
        Rules.requireNonNegative("MaxAttempts", maxAttempts);
        Rules.requirePositive("MaxDelaySeconds", maxDelaySeconds);
        Rules.requireMinimum("BackoffRate", backoffRate, 1.0);
    }

    /**
     * AWS's recommended retry policy for transient Lambda service
     * exceptions — the failures where nothing is wrong with the function,
     * the service hiccuped — plus
     * {@link Errors.Lambda#TOO_MANY_REQUESTS_EXCEPTION}, which AWS's
     * published snippet omits despite throttling being the most common
     * transient of all.  Waits of 2, 4, 8, 16, 32, and 64 seconds: six
     * attempts across about two minutes.
     *
     * Use as-is, or as a template:
     * {@code Retrier.LAMBDA_TRANSIENT.toBuilder().maxAttempts(3).build()}.
     * Retriers are scanned in order, so place this before any broader
     * Retrier, and "States.ALL" must remain last.
     *
     * @see <a href="https://docs.aws.amazon.com/step-functions/latest/dg/sfn-best-practices.html#bp-lambda-serviceexception">Handle transient Lambda service exceptions</a>
     */
    public static final Retrier LAMBDA_TRANSIENT = Retrier.builder()
            .error(Errors.Lambda.CLIENT_EXECUTION_TIMEOUT_EXCEPTION)
            .error(Errors.Lambda.SERVICE_EXCEPTION)
            .error(Errors.Lambda.AWS_LAMBDA_EXCEPTION)
            .error(Errors.Lambda.SDK_CLIENT_EXCEPTION)
            .error(Errors.Lambda.TOO_MANY_REQUESTS_EXCEPTION)
            .intervalSeconds(2)
            .maxAttempts(6)
            .backoffRate(2.0)
            .build();

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
         * The computed retry interval is used as is; the AWS default
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
