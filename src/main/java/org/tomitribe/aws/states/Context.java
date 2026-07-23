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

import jakarta.json.JsonValue;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.json.bind.annotation.JsonbProperty;
import lombok.Builder;

import java.net.URI;
import java.time.OffsetDateTime;

/**
 * The AWS Step Functions Context Object, readable in any JSONata-accepting
 * field as {@code $states.context}.  The States Language deliberately
 * leaves the Context Object's contents to the interpreter, with one
 * exception it mandates: {@code Map.Item} during a Map State's item
 * processing.  Everything else here is AWS's definition.
 *
 * Every path:
 * <pre>
 * $states.context.Execution.Id
 * $states.context.Execution.Name
 * $states.context.Execution.Input
 * $states.context.Execution.StartTime
 * $states.context.Execution.RoleArn
 * $states.context.Execution.RedriveCount
 * $states.context.Execution.RedriveTime
 * $states.context.State.Name
 * $states.context.State.EnteredTime
 * $states.context.State.RetryCount
 * $states.context.StateMachine.Id
 * $states.context.StateMachine.Name
 * $states.context.Task.Token
 * $states.context.Map.Item.Index
 * $states.context.Map.Item.Value
 * </pre>
 *
 * To stamp the whole Context Object minus the potentially large original
 * execution input:
 * <pre>
 * $states.context ~&gt; |Execution|{}, ['Input']|
 * </pre>
 *
 * @param execution the execution this state runs in; always present
 * @param state the currently executing state; always present
 * @param stateMachine the machine being executed; always present
 * @param task present only in Task States using the .waitForTaskToken
 *             integration pattern
 * @param map present only while a Map State is processing items
 * @see <a href="https://states-language.net/spec.html#context-object">The Context Object</a>
 */
@Builder(toBuilder = true, builderClassName = "Builder")
public record Context(@JsonbProperty("Execution") Execution execution,
                      @JsonbProperty("State") State state,
                      @JsonbProperty("StateMachine") StateMachine stateMachine,
                      @JsonbProperty("Task") Task task,
                      @JsonbProperty("Map") Map map) {

    /**
     * AWS writes timestamps as fixed-width RFC3339 with milliseconds, for
     * example "2019-03-26T20:14:13.192Z"; default OffsetDateTime
     * serialization drops zero-valued trailing units, so the format is
     * pinned to keep writes byte-faithful to AWS
     */
    public static final String AWS_TIMESTAMP = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    /**
     * The execution this state runs in, {@code $states.context.Execution}
     *
     * @param id the execution ARN — {@code $states.context.Execution.Id}
     * @param name the execution name, the natural correlation id —
     *             {@code $states.context.Execution.Name}
     * @param input the original execution input, readable from any state —
     *              {@code $states.context.Execution.Input}
     * @param startTime when the execution started —
     *                  {@code $states.context.Execution.StartTime}
     * @param roleArn the execution's IAM role —
     *                {@code $states.context.Execution.RoleArn}
     * @param redriveCount times this execution has been redriven, zero on
     *                     a first run — {@code $states.context.Execution.RedriveCount}
     * @param redriveTime when the execution was redriven; present only
     *                    after a redrive — {@code $states.context.Execution.RedriveTime}
     */
    @lombok.Builder(toBuilder = true, builderClassName = "Builder")
    public record Execution(@JsonbProperty("Id") URI id,
                            @JsonbProperty("Name") String name,
                            @JsonbProperty("Input") JsonValue input,
                            @JsonbProperty("StartTime") @JsonbDateFormat(Context.AWS_TIMESTAMP) OffsetDateTime startTime,
                            @JsonbProperty("RoleArn") URI roleArn,
                            @JsonbProperty("RedriveCount") Integer redriveCount,
                            @JsonbProperty("RedriveTime") @JsonbDateFormat(Context.AWS_TIMESTAMP) OffsetDateTime redriveTime) {
    }

    /**
     * The currently executing state, {@code $states.context.State}
     *
     * @param name the state's name, the natural dispatch key —
     *             {@code $states.context.State.Name}
     * @param enteredTime when the state was entered —
     *                    {@code $states.context.State.EnteredTime}
     * @param retryCount which retry attempt this is, zero on the first
     *                   attempt — {@code $states.context.State.RetryCount}
     */
    @lombok.Builder(toBuilder = true, builderClassName = "Builder")
    public record State(@JsonbProperty("Name") String name,
                        @JsonbProperty("EnteredTime") @JsonbDateFormat(Context.AWS_TIMESTAMP) OffsetDateTime enteredTime,
                        @JsonbProperty("RetryCount") Integer retryCount) {
    }

    /**
     * The machine being executed, {@code $states.context.StateMachine}
     *
     * @param id the state machine ARN — {@code $states.context.StateMachine.Id}
     * @param name the deployed machine name, which can drift from a
     *             logical name across versioned deployments —
     *             {@code $states.context.StateMachine.Name}
     */
    @lombok.Builder(toBuilder = true, builderClassName = "Builder")
    public record StateMachine(@JsonbProperty("Id") URI id,
                               @JsonbProperty("Name") String name) {
    }

    /**
     * Present only in Task States using the .waitForTaskToken integration
     * pattern, {@code $states.context.Task}
     *
     * @param token the callback token to pass to SendTaskSuccess or
     *              SendTaskFailure; treat as a credential in logs —
     *              {@code $states.context.Task.Token}
     */
    @lombok.Builder(toBuilder = true, builderClassName = "Builder")
    public record Task(@JsonbProperty("Token") String token) {
    }

    /**
     * Present only while a Map State is processing items,
     * {@code $states.context.Map}.  This is the one corner of the Context
     * Object the States Language spec mandates rather than leaving to the
     * interpreter.
     *
     * @param item the item being processed
     */
    @lombok.Builder(toBuilder = true, builderClassName = "Builder")
    public record Map(@JsonbProperty("Item") Item item) {

        /**
         * The item a Map iteration is processing
         *
         * @param index the zero-based position in the Items Array —
         *              {@code $states.context.Map.Item.Index}
         * @param value the Items Array element being processed —
         *              {@code $states.context.Map.Item.Value}
         */
        @lombok.Builder(toBuilder = true, builderClassName = "Builder")
        public record Item(@JsonbProperty("Index") Integer index,
                           @JsonbProperty("Value") JsonValue value) {
        }
    }
}
