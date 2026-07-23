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

import jakarta.json.bind.annotation.JsonbSubtype;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.json.bind.annotation.JsonbTypeInfo;

/**
 * A single state in an Amazon States Language state machine.  The state's
 * type is carried in the "Type" field of the JSON document and determines
 * which of the eight permitted implementations is used.
 *
 * Every state has a type, and may have a comment and a query language
 * override, so those are exposed here for callers walking a machine without
 * caring which state type they hold.
 *
 * This model represents the JSONata dialect of the States Language.  Fields
 * specific to the JSONPath dialect (InputPath, Parameters, ResultSelector,
 * ResultPath, OutputPath and friends) are intentionally not modeled.
 *
 * @see <a href="https://states-language.net/spec.html#state-types">State Types</a>
 */
@JsonbTypeInfo(key = "Type", value = {
        @JsonbSubtype(alias = "Task", type = TaskState.class),
        @JsonbSubtype(alias = "Parallel", type = ParallelState.class),
        @JsonbSubtype(alias = "Map", type = MapState.class),
        @JsonbSubtype(alias = "Pass", type = PassState.class),
        @JsonbSubtype(alias = "Wait", type = WaitState.class),
        @JsonbSubtype(alias = "Choice", type = ChoiceState.class),
        @JsonbSubtype(alias = "Succeed", type = SucceedState.class),
        @JsonbSubtype(alias = "Fail", type = FailState.class)
})
public sealed interface State permits TaskState, ParallelState, MapState, PassState,
        WaitState, ChoiceState, SucceedState, FailState {

    /**
     * Human-readable description of the state, the "Comment" field
     */
    String comment();

    /**
     * The state's query language override, the "QueryLanguage" field, or
     * null to use the state machine's query language
     */
    String queryLanguage();

    /**
     * The value of the "Type" field for this state.  Transient because the
     * interpreter derives it from the concrete type; JSON-B writes it via
     * the {@code @JsonbTypeInfo} discriminator instead.
     */
    @JsonbTransient
    default String type() {
        if (this instanceof TaskState) return "Task";
        if (this instanceof ParallelState) return "Parallel";
        if (this instanceof MapState) return "Map";
        if (this instanceof PassState) return "Pass";
        if (this instanceof WaitState) return "Wait";
        if (this instanceof ChoiceState) return "Choice";
        if (this instanceof SucceedState) return "Succeed";
        return "Fail";
    }
}
