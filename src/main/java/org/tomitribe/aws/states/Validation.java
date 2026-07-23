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
