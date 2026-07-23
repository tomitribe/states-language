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

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.annotation.JsonbSubtype;
import jakarta.json.bind.annotation.JsonbTypeInfo;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Guards State.type() against the {@code @JsonbSubtype} aliases — the two
 * lists live side by side in State.java and must not drift — and asserts
 * the transient type() method never leaks into the serialized document.
 */
class StateTypeTest {

    @Test
    public void typeMatchesSubtypeAlias() {
        for (final State state : states()) {
            final String alias = alias(state.getClass());
            assertEquals(alias, state.type(), state.getClass().getSimpleName());
        }
    }

    @Test
    public void typeIsNotAProperty() throws Exception {
        for (final State state : states()) {
            try (Jsonb jsonb = JsonbBuilder.create()) {
                final String json = jsonb.toJson(state, State.class);
                assertFalse(json.contains("\"type\""),
                        String.format("type() leaked into json for %s: %s",
                                state.getClass().getSimpleName(), json));
            }
        }
    }

    private List<State> states() {
        return List.of(
                TaskState.builder().resource("arn:test").end(true).build(),
                ParallelState.builder().branch(Branch.builder()
                        .startAt("A")
                        .states(Map.of())
                        .build()).end(true).build(),
                MapState.builder().itemProcessor(ItemProcessor.builder()
                        .startAt("A")
                        .states(Map.of())
                        .build()).end(true).build(),
                PassState.builder().end(true).build(),
                WaitState.builder().seconds(1).end(true).build(),
                ChoiceState.builder().choice(ChoiceRule.builder()
                        .condition(Condition.of(true))
                        .next("A")
                        .build()).build(),
                SucceedState.builder().build(),
                FailState.builder().build()
        );
    }

    private String alias(final Class<?> stateClass) {
        for (final JsonbSubtype subtype : State.class.getAnnotation(JsonbTypeInfo.class).value()) {
            if (subtype.type().equals(stateClass)) return subtype.alias();
        }
        throw new IllegalStateException("No @JsonbSubtype alias for " + stateClass.getName());
    }
}
