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
