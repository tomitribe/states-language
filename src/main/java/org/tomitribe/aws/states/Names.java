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

/**
 * States Language naming rules, enforced from the record constructors so
 * both built and unmarshalled machines are checked.  Deliberately not
 * public: callers exercise these rules by constructing model objects, not
 * by calling validation methods.
 *
 * The spec counts name lengths in Unicode characters, so limits are
 * checked as code points: a name of 50 emoji is 50 characters even though
 * it is 100 Java chars.
 *
 * @see <a href="https://states-language.net/spec.html#states-fields">States</a>
 * @see <a href="https://states-language.net/spec.html#state-machine-variables">State Machine Variables</a>
 */
final class Names {

    private Names() {
    }

    /**
     * Asserts a state name is at most 80 Unicode characters.  Uniqueness,
     * the other state naming rule, is structural: names are Map keys.
     */
    static String requireValidStateName(final String name) {
        final int length = name.codePointCount(0, name.length());
        if (length > 80) {
            throw new IllegalArgumentException(String.format(
                    "State name is %s characters long, exceeding the States Language maximum of 80: \"%s\"",
                    length, name));
        }
        return name;
    }

    /**
     * Asserts a variable name is at most 80 Unicode characters, is not the
     * reserved name "states", and is a valid Unicode identifier: an
     * ID_Start character followed by ID_Continue characters.
     */
    static String requireValidVariableName(final String name) {
        final int length = name.codePointCount(0, name.length());
        if (length > 80) {
            throw new IllegalArgumentException(String.format(
                    "Variable name is %s characters long, exceeding the States Language maximum of 80: \"%s\"",
                    length, name));
        }
        if ("states".equals(name)) {
            throw new IllegalArgumentException(
                    "Variable name \"states\" is reserved by the States Language and cannot be assigned");
        }
        if (!isIdentifier(name)) {
            throw new IllegalArgumentException(String.format(
                    "Variable name is not a valid Unicode identifier: \"%s\".  The first character must be"
                            + " a Unicode ID_Start character and the rest ID_Continue characters",
                    name));
        }
        return name;
    }

    private static boolean isIdentifier(final String name) {
        if (name.isEmpty()) return false;
        if (!Character.isUnicodeIdentifierStart(name.codePointAt(0))) return false;
        return name.codePoints().skip(1).allMatch(Character::isUnicodeIdentifierPart);
    }
}
