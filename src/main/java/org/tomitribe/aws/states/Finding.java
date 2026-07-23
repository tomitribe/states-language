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
