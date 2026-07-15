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

import com.tomitribe.aureto.states.Retrier.JitterStrategy;
import com.tomitribe.aureto.states.Retrier.MaxAttempts;
import com.tomitribe.aureto.states.test.JsonAsserts;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The named Retrier values: MaxAttempts.NEVER for the spec's
 * zero-means-never-retry rule, and the AWS-defined jitter strategies.
 */
class RetrierTest {

    @Test
    public void maxAttemptsNever() throws Exception {
        final Retrier retrier = Retrier.builder()
                .error(Errors.States.TIMEOUT)
                .maxAttempts(MaxAttempts.NEVER)
                .build();

        try (Jsonb jsonb = JsonbBuilder.create()) {
            JsonAsserts.assertJson("""
                    {
                      "ErrorEquals": ["States.Timeout"],
                      "MaxAttempts": 0
                    }""", jsonb.toJson(retrier));
        }
    }

    @Test
    public void lambdaTransient() throws Exception {
        try (Jsonb jsonb = JsonbBuilder.create()) {
            JsonAsserts.assertJson("""
                    {
                      "ErrorEquals": [
                        "Lambda.ClientExecutionTimeoutException",
                        "Lambda.ServiceException",
                        "Lambda.AWSLambdaException",
                        "Lambda.SdkClientException",
                        "Lambda.TooManyRequestsException"
                      ],
                      "IntervalSeconds": 2,
                      "MaxAttempts": 6,
                      "BackoffRate": 2.0
                    }""", jsonb.toJson(Retrier.LAMBDA_TRANSIENT));
        }
    }

    /**
     * The constant doubles as a template: tweak through toBuilder()
     * without disturbing the shared instance
     */
    @Test
    public void lambdaTransientIsATemplate() {
        final Retrier tweaked = Retrier.LAMBDA_TRANSIENT.toBuilder()
                .maxAttempts(3)
                .build();

        assertEquals(3, tweaked.maxAttempts());
        assertEquals(5, tweaked.errorEquals().size());
        assertEquals(6, Retrier.LAMBDA_TRANSIENT.maxAttempts());
    }

    @Test
    public void jitterStrategies() throws Exception {
        final Retrier retrier = Retrier.builder()
                .errorEquals(List.of("States.ALL"))
                .intervalSeconds(2)
                .maxAttempts(2)
                .backoffRate(2.0)
                .jitterStrategy(JitterStrategy.FULL)
                .build();

        try (Jsonb jsonb = JsonbBuilder.create()) {
            JsonAsserts.assertJson("""
                    {
                      "ErrorEquals": ["States.ALL"],
                      "IntervalSeconds": 2,
                      "MaxAttempts": 2,
                      "BackoffRate": 2.0,
                      "JitterStrategy": "FULL"
                    }""", jsonb.toJson(retrier));
        }
    }
}
