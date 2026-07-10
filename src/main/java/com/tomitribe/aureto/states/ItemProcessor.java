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
import jakarta.json.bind.annotation.JsonbProperty;
import lombok.Builder;

import java.util.Map;

/**
 * The state machine a Map State executes once per item or batch of items.
 * The {@code startAt} and {@code states} fields work exactly like those at
 * the top level of a state machine.  States in an ItemProcessor can
 * transition only to each other, and no state outside can transition in.
 *
 * @param processorConfig interpreter-defined execution configuration, for
 *                        example AWS's {@code {"Mode": "DISTRIBUTED", "ExecutionType": "STANDARD"}}
 * @param startAt name of the state execution begins at; must exactly match a key in {@code states}
 * @param states the states of the processor, keyed by state name
 * @see <a href="https://states-language.net/spec.html#map-state-itemprocessor-definition">Map State ItemProcessor definition</a>
 */
@Builder(toBuilder = true, builderClassName = "Builder")
public record ItemProcessor(@JsonbProperty("ProcessorConfig") JsonObject processorConfig,
                            @JsonbProperty("StartAt") String startAt,
                            @JsonbProperty("States") Map<String, State> states) {
    public ItemProcessor {
        ValidCheck.requireNotNull(startAt, "startAt");
        ValidCheck.requireNotNull(states, "states");
    }
}
