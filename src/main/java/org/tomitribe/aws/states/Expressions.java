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
