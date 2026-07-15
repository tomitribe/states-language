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

import lombok.Builder;
import lombok.Singular;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The result of validating a whole state machine document through
 * {@link StateMachine#validate()}: the rules decidable only with the
 * entire machine in hand, as opposed to the single-object rules the
 * record constructors enforce.
 *
 * @param findings everything found, in document order
 */
@Builder(toBuilder = true, builderClassName = "Builder")
public record Validation(@Singular List<Finding> findings) {

    public Validation {
        findings = findings == null ? List.of() : List.copyOf(findings);
    }

    public boolean isValid() {
        return errors().isEmpty();
    }

    public List<Finding> errors() {
        return findings.stream()
                .filter(finding -> finding.severity() == Finding.Severity.ERROR)
                .toList();
    }

    public List<Finding> warnings() {
        return findings.stream()
                .filter(finding -> finding.severity() == Finding.Severity.WARNING)
                .toList();
    }

    /**
     * Throws when the machine has any error-severity findings, listing
     * every one so a generator failure reports all problems at once
     */
    public Validation requireValid() {
        if (isValid()) return this;
        throw new IllegalStateException(errors().stream()
                .map(finding -> finding.path() + ": " + finding.message())
                .collect(Collectors.joining("\n", "The state machine is not valid:\n", "")));
    }
}
