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

import jakarta.json.JsonValue;
import lombok.Builder;

/**
 * The literal form of "Condition": a fixed boolean.  A rule with a literal
 * true condition always matches, which is useful as the last rule of a
 * Choice in place of a "Default" transition.
 *
 * @param value the literal condition
 * @see <a href="https://states-language.net/spec.html#jsonata-choice-rules">JSONata Choice Rules</a>
 */
@Builder(toBuilder = true, builderClassName = "Builder")
public record ConditionBoolean(boolean value) implements Condition {

    @Override
    public JsonValue toJsonValue() {
        return value ? JsonValue.TRUE : JsonValue.FALSE;
    }
}
