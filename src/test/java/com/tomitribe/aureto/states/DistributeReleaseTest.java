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

import com.tomitribe.aureto.states.test.JsonAsserts;
import jakarta.json.Json;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

/**
 * Builds the release distribution machine entirely through the builder API
 * and asserts the serialized document matches the expected ASL, proving the
 * model can author documents and not merely round-trip them.
 */
class DistributeReleaseTest {

    private static final String ENVELOPE = "{% $merge([$states.input, {'step': $states.context.State.Name}]) %}";
    private static final String CATCH_ERROR = "{% $merge([$states.input, {'error': $states.errorOutput}]) %}";
    private static final String FUNCTION = "arn:aws:lambda:us-east-1:123456789012:function:";

    @Test
    public void distributeRelease() throws Exception {
        final TaskState expandCustomers = TaskState.builder()
                .comment("Produces one item of work per customer entitled to this release")
                .resource(FUNCTION + "ExpandCustomers")
                .arguments(Json.createValue(ENVELOPE))
                .assign(Json.createObjectBuilder()
                        .add("product", "{% $states.input.product %}")
                        .add("version", "{% $states.input.version %}")
                        .build())
                .timeoutSeconds(300)
                .next("DistributeToCustomers")
                .build();

        final Catcher markFailed = Catcher.builder()
                .errorEquals(List.of("States.ALL"))
                .output(Json.createValue(CATCH_ERROR))
                .next("MarkFailed")
                .build();

        final TaskState weaveRelease = TaskState.builder()
                .resource(FUNCTION + "WeaveRelease")
                .arguments(Json.createValue(ENVELOPE))
                .retry(List.of(Retrier.builder()
                        .errorEquals(List.of("States.Timeout"))
                        .intervalSeconds(3)
                        .maxAttempts(2)
                        .backoffRate(2.0)
                        .build()))
                .catchers(List.of(markFailed))
                .next("PublishRelease")
                .build();

        final TaskState publishRelease = TaskState.builder()
                .resource(FUNCTION + "PublishRelease")
                .arguments(Json.createValue(ENVELOPE))
                .catchers(List.of(markFailed))
                .end(true)
                .build();

        final TaskState markFailedTask = TaskState.builder()
                .resource(FUNCTION + "MarkFailed")
                .arguments(Json.createValue(ENVELOPE))
                .end(true)
                .build();

        final MapState distributeToCustomers = MapState.builder()
                .items(Json.createValue("{% $states.input %}"))
                .maxConcurrency(40)
                .itemProcessor(ItemProcessor.builder()
                        .startAt("WeaveRelease")
                        .states(Map.of(
                                "WeaveRelease", weaveRelease,
                                "PublishRelease", publishRelease,
                                "MarkFailed", markFailedTask))
                        .build())
                .next("SendSummary")
                .build();

        final TaskState sendSummary = TaskState.builder()
                .resource(FUNCTION + "SendSummary")
                .arguments(Json.createObjectBuilder()
                        .add("step", "{% $states.context.State.Name %}")
                        .add("total", "{% $count($states.input) %}")
                        .add("results", "{% $states.input %}")
                        .build())
                .end(true)
                .build();

        final StateMachine machine = StateMachine.builder()
                .comment("Distribute a release to all customers and email a summary")
                .queryLanguage("JSONata")
                .startAt("ExpandCustomers")
                .states(Map.of(
                        "ExpandCustomers", expandCustomers,
                        "DistributeToCustomers", distributeToCustomers,
                        "SendSummary", sendSummary))
                .build();

        final String expected = JsonAsserts.resource("DistributeReleaseTest/DistributeRelease.json");
        JsonAsserts.assertJsonbReadWrite(machine, expected);
    }
}
