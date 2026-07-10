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

import jakarta.json.bind.annotation.JsonbSubtype;
import jakarta.json.bind.annotation.JsonbTypeInfo;

/**
 * A single state in an Amazon States Language state machine.  The state's
 * type is carried in the "Type" field of the JSON document and determines
 * which of the eight permitted implementations is used.
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
}
