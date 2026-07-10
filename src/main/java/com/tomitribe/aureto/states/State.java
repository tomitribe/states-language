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
