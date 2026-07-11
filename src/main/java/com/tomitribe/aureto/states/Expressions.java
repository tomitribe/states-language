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

/**
 * The "{%" and "%}" delimiters that mark a string as a JSONata expression.
 * Deliberately not public: callers express the literal-or-expression
 * distinction through the model types, never by handling delimiters.
 *
 * @see <a href="https://states-language.net/spec.html#expressions">JSONata Expressions</a>
 */
final class Expressions {

    private Expressions() {
    }

    static boolean isDelimited(final String value) {
        return value.startsWith("{%") && value.endsWith("%}");
    }

    static String delimit(final String expression) {
        return "{% " + expression + " %}";
    }

    static String strip(final String delimited) {
        return delimited.substring(2, delimited.length() - 2).trim();
    }
}
