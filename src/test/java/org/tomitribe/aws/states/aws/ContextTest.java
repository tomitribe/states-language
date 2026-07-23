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
package org.tomitribe.aws.states.aws;

import org.tomitribe.aws.states.test.JsonAsserts;
import jakarta.json.Json;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ContextTest {

    private static final String FULL = """
            {
              "Execution": {
                "Id": "arn:aws:states:us-east-1:123456789012:execution:distribute-release:run-42",
                "Name": "run-42",
                "Input": { "product": "tomcat", "version": "10.1.2-TT.1" },
                "StartTime": "2026-07-14T09:15:13.192Z",
                "RoleArn": "arn:aws:iam::123456789012:role/DistributeRelease",
                "RedriveCount": 0
              },
              "State": {
                "Name": "WeaveRelease",
                "EnteredTime": "2026-07-14T09:15:14.001Z",
                "RetryCount": 2
              },
              "StateMachine": {
                "Id": "arn:aws:states:us-east-1:123456789012:stateMachine:distribute-release",
                "Name": "distribute-release"
              },
              "Task": {
                "Token": "AQB8example"
              },
              "Map": {
                "Item": {
                  "Index": 3,
                  "Value": { "customer": "12345678" }
                }
              }
            }""";

    @Test
    public void fullContextRoundTrips() throws Exception {
        JsonAsserts.assertJsonb(FULL, Context.class);
    }

    @Test
    public void typedAccess() throws Exception {
        final Context context;
        try (Jsonb jsonb = JsonbBuilder.create()) {
            context = jsonb.fromJson(FULL, Context.class);
        }

        assertEquals("run-42", context.execution().name());
        assertEquals(OffsetDateTime.parse("2026-07-14T09:15:13.192Z"), context.execution().startTime());
        assertEquals(URI.create("arn:aws:iam::123456789012:role/DistributeRelease"),
                context.execution().roleArn());
        assertEquals("WeaveRelease", context.state().name());
        assertEquals(2, context.state().retryCount());
        assertEquals("distribute-release", context.stateMachine().name());
        assertEquals("AQB8example", context.task().token());
        assertEquals(3, context.map().item().index());
        assertEquals("12345678", context.map().item().value().asJsonObject().getString("customer"));
    }

    /**
     * Task appears only in callback-pattern tasks and Map only during item
     * processing; a context without them round-trips with nothing invented
     */
    @Test
    public void partialContextRoundTrips() throws Exception {
        final String partial = """
                {
                  "Execution": {
                    "Id": "arn:aws:states:us-east-1:123456789012:execution:distribute-release:run-42",
                    "Name": "run-42",
                    "StartTime": "2026-07-14T09:15:13.192Z"
                  },
                  "State": {
                    "Name": "SendSummary",
                    "EnteredTime": "2026-07-14T09:20:00.000Z",
                    "RetryCount": 0
                  },
                  "StateMachine": {
                    "Id": "arn:aws:states:us-east-1:123456789012:stateMachine:distribute-release",
                    "Name": "distribute-release"
                  }
                }""";

        JsonAsserts.assertJsonb(partial, Context.class);

        try (Jsonb jsonb = JsonbBuilder.create()) {
            final Context context = jsonb.fromJson(partial, Context.class);
            assertNull(context.task());
            assertNull(context.map());
        }
    }

    @Test
    public void buildersWriteTheAwsShape() throws Exception {
        final Context context = Context.builder()
                .execution(Context.Execution.builder()
                        .name("run-42")
                        .startTime(OffsetDateTime.parse("2026-07-14T09:15:13.192Z"))
                        .build())
                .state(Context.State.builder()
                        .name("WeaveRelease")
                        .retryCount(0)
                        .build())
                .map(Context.Map.builder()
                        .item(Context.Map.Item.builder()
                                .index(3)
                                .value(Json.createObjectBuilder().add("customer", "12345678").build())
                                .build())
                        .build())
                .build();

        JsonAsserts.assertJsonbReadWrite(context, """
                {
                  "Execution": {
                    "Name": "run-42",
                    "StartTime": "2026-07-14T09:15:13.192Z"
                  },
                  "State": {
                    "Name": "WeaveRelease",
                    "RetryCount": 0
                  },
                  "Map": {
                    "Item": {
                      "Index": 3,
                      "Value": { "customer": "12345678" }
                    }
                  }
                }""");
    }
}
