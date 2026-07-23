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

import io.github.aglibs.validcheck.ValidCheck;
import jakarta.json.JsonObject;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTypeAdapter;
import lombok.Builder;
import lombok.Singular;

import java.net.URI;
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
                        @JsonbProperty("Resource") URI resource,
                        @JsonbProperty("Arguments") @JsonbTypeAdapter(Arguments.Adapter.class) Arguments arguments,
                        @JsonbProperty("Output") @JsonbTypeAdapter(Output.Adapter.class) Output output,
                        @JsonbProperty("Assign") @JsonbTypeAdapter(Assign.Adapter.class) Assign assign,
                        @JsonbProperty("Retry") @Singular("retrier") List<Retrier> retry,
                        @JsonbProperty("Catch") @Singular("catcher") List<Catcher> catchers,
                        @JsonbProperty("TimeoutSeconds") Integer timeoutSeconds,
                        @JsonbProperty("HeartbeatSeconds") Integer heartbeatSeconds,
                        @JsonbProperty("Credentials") JsonObject credentials,
                        @JsonbProperty("Next") String next,
                        @JsonbProperty("End") Boolean end) implements State {
    public TaskState {
        ValidCheck.requireNotNull(resource, "resource");
        Rules.requireTransition(TaskState.class, next, end);
        Rules.requirePositive("TimeoutSeconds", timeoutSeconds);
        Rules.requirePositive("HeartbeatSeconds", heartbeatSeconds);
        if (timeoutSeconds != null && heartbeatSeconds != null && heartbeatSeconds >= timeoutSeconds) {
            throw new IllegalArgumentException(String.format(
                    "\"HeartbeatSeconds\" (%s) must be smaller than \"TimeoutSeconds\" (%s)",
                    heartbeatSeconds, timeoutSeconds));
        }
        retry = retry == null || retry.isEmpty() ? null : List.copyOf(retry);
        catchers = catchers == null || catchers.isEmpty() ? null : List.copyOf(catchers);
        Rules.requireStatesAllPlacement(retry, catchers);
    }

    public static class Builder {

        // Lombok skips generating any setter whose name is hand-written,
        // signatures notwithstanding, so declaring the String convenience
        // requires declaring the URI form as well
        public Builder resource(final URI resource) {
            this.resource = resource;
            return this;
        }

        /**
         * Convenience for the common case of a resource URI in hand as a
         * string, for example an ARN.  Fails immediately if the value is
         * not a syntactically valid URI.
         */
        public Builder resource(final String resource) {
            return resource(URI.create(resource));
        }
    }
}
