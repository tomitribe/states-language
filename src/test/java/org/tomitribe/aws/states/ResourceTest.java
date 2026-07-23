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

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The spec says a Resource "MUST be a URI that uniquely identifies the
 * specific task to execute" with no constraint on scheme, so the model
 * types it as java.net.URI.  The builders keep a String convenience
 * overload that validates on the spot.
 */
class ResourceTest {

    private static final String ARN = "arn:aws:lambda:us-east-1:123456789012:function:WeaveRelease";

    @Test
    public void arnsAreValidUris() {
        final TaskState task = TaskState.builder()
                .resource(ARN)
                .end(true)
                .build();

        assertEquals(URI.create(ARN), task.resource());
        assertEquals("arn", task.resource().getScheme());
    }

    @Test
    public void urisArePassedDirectly() {
        final TaskState task = TaskState.builder()
                .resource(URI.create(ARN))
                .end(true)
                .build();

        assertEquals(ARN, task.resource().toString());
    }

    @Test
    public void invalidUrisAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> TaskState.builder().resource("arn:aws:lambda:not a uri"));
        assertThrows(IllegalArgumentException.class,
                () -> ItemReader.builder().resource("arn:aws:states:{bad}"));
        assertThrows(IllegalArgumentException.class,
                () -> ResultWriter.builder().resource("arn:aws:states:{bad}"));
    }
}
