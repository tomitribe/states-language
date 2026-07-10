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

import io.github.aglibs.validcheck.ValidCheck;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.json.bind.annotation.JsonbProperty;
import lombok.Builder;
import lombok.Singular;

import java.util.List;

/**
 * Executes the work identified by the {@code resource} URI.  The States
 * Language does not constrain the URI scheme; on AWS this is typically a
 * Lambda function or service integration ARN.
 *
 * If {@code arguments} is absent the task receives the state input.  If
 * {@code output} is absent the state output is the task's result.
 *
 * @param comment human-readable description of the state
 * @param queryLanguage overrides the state machine's query language; this model targets JSONata
 * @param resource URI uniquely identifying the task to execute; required
 * @param arguments input to the task; a JSON value or a JSONata string, defaults to the state input
 * @param output the state output; a JSON value or a JSONata string, may reference $states.result
 * @param assign variable assignments; values may be JSONata strings, effective in the next state
 * @param retry retry policies scanned in order when the task reports an error
 * @param catchers fallback transitions scanned in order when retries are exhausted
 * @param timeoutSeconds maximum seconds the task may run, default 60; fails with States.Timeout
 * @param heartbeatSeconds maximum seconds between heartbeats; must be smaller than timeoutSeconds
 * @param credentials interpreter-defined credentials used to execute the resource
 * @param next name of the state to transition to; exactly one of next or end is required
 * @param end true marks this state as terminal
 * @see <a href="https://states-language.net/spec.html#task-state">Task State</a>
 */
@Builder(toBuilder = true, builderClassName = "Builder")
public record TaskState(@JsonbProperty("Comment") String comment,
                        @JsonbProperty("QueryLanguage") String queryLanguage,
                        @JsonbProperty("Resource") String resource,
                        @JsonbProperty("Arguments") JsonValue arguments,
                        @JsonbProperty("Output") JsonValue output,
                        @JsonbProperty("Assign") JsonObject assign,
                        @JsonbProperty("Retry") @Singular("retrier") List<Retrier> retry,
                        @JsonbProperty("Catch") @Singular("catcher") List<Catcher> catchers,
                        @JsonbProperty("TimeoutSeconds") Integer timeoutSeconds,
                        @JsonbProperty("HeartbeatSeconds") Integer heartbeatSeconds,
                        @JsonbProperty("Credentials") JsonObject credentials,
                        @JsonbProperty("Next") String next,
                        @JsonbProperty("End") Boolean end) implements State {
    public TaskState {
        ValidCheck.requireNotNull(resource, "resource");
        retry = retry == null || retry.isEmpty() ? null : List.copyOf(retry);
        catchers = catchers == null || catchers.isEmpty() ? null : List.copyOf(catchers);
    }
}
