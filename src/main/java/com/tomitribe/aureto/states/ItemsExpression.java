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
import jakarta.json.Json;
import jakarta.json.JsonValue;
import lombok.Builder;

/**
 * The expression form of "Items": a single JSONata expression that must
 * produce the Items Array, for example
 * {@code Items.expression("$states.input.detail.shipped")}.
 *
 * The expression is held without its "{%" and "%}" delimiters; they are
 * supplied on serialization and stripped on deserialization.
 *
 * @param expression the bare expression, without the "{%" and "%}" delimiters
 * @see <a href="https://states-language.net/spec.html#selecting-items">Selecting Items</a>
 */
@Builder(toBuilder = true, builderClassName = "Builder")
public record ItemsExpression(String expression) implements Items {

    public ItemsExpression {
        ValidCheck.requireNotNull(expression, "expression");
        if (Expressions.isDelimited(expression)) {
            throw new IllegalArgumentException(String.format(
                    "Expression is already delimited: \"%s\"."
                            + "  Pass the expression without the {%% %%} delimiters",
                    expression));
        }
    }

    @Override
    public JsonValue toJsonValue() {
        return Json.createValue(Expressions.delimit(expression));
    }
}
