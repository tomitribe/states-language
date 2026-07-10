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

import java.util.List;

/**
 * Adds branching logic to a state machine.  The interpreter scans the Choice
 * Rules in order and transitions to the first rule whose condition evaluates
 * to true, or to {@code defaultState} if none match.  A Choice State that
 * matches no rule and has no default fails with States.NoChoiceMatched.
 *
 * A Choice State must not be an End state, so it carries no next or end
 * fields of its own; all transitions are named by the rules and the default.
 *
 * The top-level {@code output} and {@code assign} fields apply only when no
 * Choice Rule matches and the default transition is taken.
 *
 * @param comment human-readable description of the state
 * @param queryLanguage overrides the state machine's query language; this model targets JSONata
 * @param choices the Choice Rules, scanned in order; required, non-empty
 * @param defaultState name of the state to execute if no rule matches
 * @param output the state output when no rule matches; a JSON value or a JSONata string
 * @param assign variable assignments evaluated when no rule matches
 * @see <a href="https://states-language.net/spec.html#choice-state">Choice State</a>
 */
@Builder(toBuilder = true)
public record ChoiceState(@JsonbProperty("Comment") String comment,
                          @JsonbProperty("QueryLanguage") String queryLanguage,
                          @JsonbProperty("Choices") List<ChoiceRule> choices,
                          @JsonbProperty("Default") String defaultState,
                          @JsonbProperty("Output") JsonValue output,
                          @JsonbProperty("Assign") JsonObject assign) implements State {
    public ChoiceState {
        ValidCheck.requireNotNull(choices, "choices");
    }
}
