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
import lombok.Builder;

/**
 * One result of validating a state machine: what was found, where, and how
 * serious it is.  Errors are rule violations that will fail on the
 * interpreter; warnings are legal documents likely to misbehave at
 * runtime.
 *
 * @param severity whether the finding blocks execution or advises
 * @param path where in the document, for example
 *             "States.Distribute.ItemProcessor.States.Weave"
 * @param message what was found and how to fix it
 */
@Builder(toBuilder = true, builderClassName = "Builder")
public record Finding(Severity severity, String path, String message) {

    public Finding {
        ValidCheck.requireNotNull(severity, "severity");
        ValidCheck.requireNotNull(path, "path");
        ValidCheck.requireNotNull(message, "message");
    }

    public static Finding error(final String path, final String message) {
        return new Finding(Severity.ERROR, path, message);
    }

    public static Finding warning(final String path, final String message) {
        return new Finding(Severity.WARNING, path, message);
    }

    public enum Severity {
        ERROR,
        WARNING
    }
}
