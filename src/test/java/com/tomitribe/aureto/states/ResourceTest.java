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
