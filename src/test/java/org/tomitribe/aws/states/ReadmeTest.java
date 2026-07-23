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

import org.tomitribe.aws.states.test.JsonAsserts;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The examples printed in README.md, verbatim — if this test compiles and
 * passes, the README cannot lie
 */
class ReadmeTest {

    @Test
    public void quickStart() throws Exception {
        final TaskState chargeCard = TaskState.builder()
                .resource("arn:aws:lambda:us-east-1:123456789012:function:ChargeCard")
                .arguments(Arguments.builder()
                        .expression("orderId", "$states.input.orderId")
                        .value("currency", "USD")
                        .build())
                .retrier(Retrier.LAMBDA_TRANSIENT)
                .catcher(Catcher.builder()
                        .error(Errors.States.ALL)
                        .output(Output.expression("$merge([$states.input, {'error': $states.errorOutput}])"))
                        .next("Refund")
                        .build())
                .next("Ship")
                .build();

        final TaskState ship = TaskState.builder()
                .resource("arn:aws:lambda:us-east-1:123456789012:function:Ship")
                .end(true)
                .build();

        final TaskState refund = TaskState.builder()
                .resource("arn:aws:lambda:us-east-1:123456789012:function:Refund")
                .end(true)
                .build();

        final StateMachine machine = StateMachine.builder()
                .comment("Charge the card, ship the order, refund on any failure")
                .queryLanguage("JSONata")
                .startAt("Charge Card")
                .states(Map.of(
                        "Charge Card", chargeCard,
                        "Ship", ship,
                        "Refund", refund))
                .build();

        machine.validate().requireValid();

        final String json;
        try (Jsonb jsonb = JsonbBuilder.create()) {
            json = jsonb.toJson(machine);
        }

        // the document the README quotes
        JsonAsserts.assertJson("""
                {
                  "Comment": "Charge the card, ship the order, refund on any failure",
                  "QueryLanguage": "JSONata",
                  "StartAt": "Charge Card",
                  "States": {
                    "Charge Card": {
                      "Type": "Task",
                      "Resource": "arn:aws:lambda:us-east-1:123456789012:function:ChargeCard",
                      "Arguments": {
                        "orderId": "{% $states.input.orderId %}",
                        "currency": "USD"
                      },
                      "Retry": [
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
                        }
                      ],
                      "Catch": [
                        {
                          "ErrorEquals": ["States.ALL"],
                          "Output": "{% $merge([$states.input, {'error': $states.errorOutput}]) %}",
                          "Next": "Refund"
                        }
                      ],
                      "Next": "Ship"
                    },
                    "Ship": {
                      "Type": "Task",
                      "Resource": "arn:aws:lambda:us-east-1:123456789012:function:Ship",
                      "End": true
                    },
                    "Refund": {
                      "Type": "Task",
                      "Resource": "arn:aws:lambda:us-east-1:123456789012:function:Refund",
                      "End": true
                    }
                  }
                }""", json);

        // and the read side round-trips it
        try (Jsonb jsonb = JsonbBuilder.create()) {
            assertEquals(machine, jsonb.fromJson(json, StateMachine.class));
        }
    }

    /**
     * The construction-time failures the README quotes
     */
    @Test
    public void illegalMachinesAreUnrepresentable() {
        final IllegalArgumentException transition = assertThrows(IllegalArgumentException.class,
                () -> TaskState.builder().resource("arn:aws:lambda:us-east-1:123456789012:function:Ship").build());
        assertEquals("TaskState must have a \"Next\" field or \"End\": true", transition.getMessage());

        final IllegalArgumentException expression = assertThrows(IllegalArgumentException.class,
                () -> Assign.builder().value("total", "{% $sum($states.input.items.price) %}"));
        assertEquals("Value for variable \"total\" looks like a JSONata expression:"
                + " \"{% $sum($states.input.items.price) %}\"."
                + "  Use expression(\"total\", \"$sum($states.input.items.price)\") instead",
                expression.getMessage());

        final IllegalArgumentException reserved = assertThrows(IllegalArgumentException.class,
                () -> Assign.builder().value("states", "nope"));
        assertEquals("Variable name \"states\" is reserved by the States Language and cannot be assigned",
                reserved.getMessage());
    }

    /**
     * The validator output the README quotes
     */
    @Test
    public void wholeDocumentValidation() {
        final StateMachine machine = StateMachine.builder()
                .queryLanguage("JSONata")
                .startAt("Charge Card")
                .states(Map.of("Charge", PassState.builder().next("Shipp").build()))
                .build();

        final Validation validation = machine.validate();

        assertEquals(2, validation.errors().size());
        assertEquals("\"StartAt\": \"Charge Card\" does not match any state in States."
                + "  States present: Charge", validation.errors().get(0).message());
        assertEquals("\"Next\": \"Shipp\" does not match any state."
                + "  States present in this scope: Charge", validation.errors().get(1).message());
        assertEquals(1, validation.warnings().size());
        assertEquals("Unreachable; no transition targets it and it is not the \"StartAt\"",
                validation.warnings().get(0).message());
    }
}
