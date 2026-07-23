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
