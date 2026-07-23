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

import org.tomitribe.aws.states.Retrier.JitterStrategy;
import org.tomitribe.aws.states.Retrier.MaxAttempts;
import org.tomitribe.aws.states.test.JsonAsserts;
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
