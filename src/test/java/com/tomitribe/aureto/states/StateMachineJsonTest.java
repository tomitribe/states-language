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
import org.junit.jupiter.api.Test;

/**
 * Round-trips complete state machine documents through the model.  Any
 * field the model fails to map is silently dropped on read and therefore
 * missing on write, which fails the comparison — so these documents
 * collectively exercise every field of every state type.
 */
class StateMachineJsonTest {

    /**
     * Task, Map, ItemProcessor, Retrier, Catcher, Assign, Arguments,
     * Items and MaxConcurrency
     */
    @Test
    public void distributeRelease() throws Exception {
        assertRoundTrip("DistributeRelease");
    }

    /**
     * Pass, Choice, ChoiceRule, Wait in both Seconds and Timestamp forms,
     * Parallel, Branch, Succeed and Fail
     */
    @Test
    public void controlFlow() throws Exception {
        assertRoundTrip("ControlFlow");
    }

    /**
     * ItemReader, ItemSelector, ItemBatcher, ResultWriter, ProcessorConfig,
     * Credentials, HeartbeatSeconds, tolerated failure fields and the
     * top-level Version and TimeoutSeconds
     */
    @Test
    public void distributedMap() throws Exception {
        assertRoundTrip("DistributedMap");
    }

    private void assertRoundTrip(final String name) throws Exception {
        final String json = JsonAsserts.resource(String.format("StateMachineJsonTest/%s.json", name));
        JsonAsserts.assertJsonb(json, StateMachine.class);
    }
}
