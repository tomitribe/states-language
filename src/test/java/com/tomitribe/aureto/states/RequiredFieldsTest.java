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

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The States Language MUST rules that are decidable from a single object's
 * own fields, enforced at construction: exactly one of Next or End: true
 * on the five transitioning states, Wait's exactly-one-of Seconds or
 * Timestamp, and the numeric range rules on Task, Map, Retrier, and
 * ItemBatcher.
 */
class RequiredFieldsTest {

    @Test
    public void statesRequireNextOrEnd() {
        final IllegalArgumentException neither = assertThrows(IllegalArgumentException.class,
                () -> TaskState.builder().resource("arn:test").build());
        assertEquals("TaskState must have a \"Next\" field or \"End\": true", neither.getMessage());

        final IllegalArgumentException both = assertThrows(IllegalArgumentException.class,
                () -> TaskState.builder().resource("arn:test").next("A").end(true).build());
        assertEquals("TaskState must not have both a \"Next\" field and \"End\": true", both.getMessage());

        assertThrows(IllegalArgumentException.class, () -> PassState.builder().build());
        assertThrows(IllegalArgumentException.class, () -> WaitState.builder().seconds(1).build());
        assertThrows(IllegalArgumentException.class, () -> ParallelState.builder()
                .branch(branch()).build());
        assertThrows(IllegalArgumentException.class, () -> MapState.builder()
                .itemProcessor(processor()).build());
    }

    /**
     * "End": false is legal noise alongside "Next", not a conflict
     */
    @Test
    public void endFalseWithNextIsLegal() {
        TaskState.builder().resource("arn:test").next("A").end(false).build();
    }

    @Test
    public void waitRequiresExactlyOneOfSecondsOrTimestamp() {
        final IllegalArgumentException neither = assertThrows(IllegalArgumentException.class,
                () -> WaitState.builder().end(true).build());
        assertEquals("WaitState must have exactly one of \"Seconds\" or \"Timestamp\"", neither.getMessage());

        assertThrows(IllegalArgumentException.class, () -> WaitState.builder()
                .seconds(10).timestamp("2026-03-14T01:59:00Z").end(true).build());
    }

    @Test
    public void taskTimeoutRules() {
        assertThrows(IllegalArgumentException.class, () -> TaskState.builder()
                .resource("arn:test").timeoutSeconds(0).end(true).build());
        assertThrows(IllegalArgumentException.class, () -> TaskState.builder()
                .resource("arn:test").heartbeatSeconds(-5).end(true).build());

        final IllegalArgumentException heartbeat = assertThrows(IllegalArgumentException.class,
                () -> TaskState.builder()
                        .resource("arn:test").timeoutSeconds(30).heartbeatSeconds(60).end(true).build());
        assertEquals("\"HeartbeatSeconds\" (60) must be smaller than \"TimeoutSeconds\" (30)",
                heartbeat.getMessage());
    }

    @Test
    public void mapRangeRules() {
        assertThrows(IllegalArgumentException.class, () -> MapState.builder()
                .itemProcessor(processor()).maxConcurrency(-1).end(true).build());
        assertThrows(IllegalArgumentException.class, () -> MapState.builder()
                .itemProcessor(processor()).toleratedFailureCount(-1).end(true).build());

        final IllegalArgumentException percentage = assertThrows(IllegalArgumentException.class,
                () -> MapState.builder()
                        .itemProcessor(processor()).toleratedFailurePercentage(150.0).end(true).build());
        assertEquals("\"ToleratedFailurePercentage\" must be a number between zero and 100: 150.0",
                percentage.getMessage());
    }

    @Test
    public void itemBatcherRequiresAMaximum() {
        final IllegalArgumentException neither = assertThrows(IllegalArgumentException.class,
                () -> ItemBatcher.builder().build());
        assertEquals("ItemBatcher must have at least one of \"MaxItemsPerBatch\""
                + " or \"MaxInputBytesPerBatch\"", neither.getMessage());

        assertThrows(IllegalArgumentException.class, () -> ItemBatcher.builder()
                .maxItemsPerBatch(0).build());
    }

    @Test
    public void retrierValueRules() {
        assertThrows(IllegalArgumentException.class, () -> Retrier.builder()
                .error("States.ALL").intervalSeconds(0).build());
        assertThrows(IllegalArgumentException.class, () -> Retrier.builder()
                .error("States.ALL").maxAttempts(-1).build());
        assertThrows(IllegalArgumentException.class, () -> Retrier.builder()
                .error("States.ALL").maxDelaySeconds(0).build());

        final IllegalArgumentException backoff = assertThrows(IllegalArgumentException.class,
                () -> Retrier.builder().error("States.ALL").backoffRate(0.5).build());
        assertEquals("\"BackoffRate\" must be greater than or equal to 1.0: 0.5", backoff.getMessage());
    }

    private Branch branch() {
        return Branch.builder().startAt("A").states(Map.of()).build();
    }

    private ItemProcessor processor() {
        return ItemProcessor.builder().startAt("A").states(Map.of()).build();
    }
}
