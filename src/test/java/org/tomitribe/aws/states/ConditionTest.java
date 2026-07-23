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

import org.tomitribe.aws.states.test.JsonAsserts;
import jakarta.json.Json;
import jakarta.json.JsonValue;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTypeAdapter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Condition is the one union whose second arm is a boolean rather than a
 * JSON structure, so its forms and adapter are covered here; the shared
 * expression mechanics are proven by ArgumentsTest.
 */
class ConditionTest {

    @Test
    public void booleanForm() {
        assertEquals("true", Condition.of(true).toJsonValue().toString());
        assertEquals("false", Condition.of(false).toJsonValue().toString());
        assertTrue(Condition.of(true).value());
    }

    @Test
    public void expressionForm() {
        final ConditionExpression ready = Condition.expression("$states.input.status = 'READY'");

        assertEquals("$states.input.status = 'READY'", ready.expression());
        assertEquals("\"{% $states.input.status = 'READY' %}\"", ready.toJsonValue().toString());
    }

    @Test
    public void adapterRestoresTheForm() throws Exception {
        final Condition.Adapter adapter = new Condition.Adapter();

        assertEquals(Condition.of(true), adapter.adaptFromJson(JsonValue.TRUE));
        assertEquals(Condition.of(false), adapter.adaptFromJson(JsonValue.FALSE));

        final ConditionExpression expression = Condition.expression("$value >= 20");
        assertEquals(expression, adapter.adaptFromJson(adapter.adaptToJson(expression)));

        final IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                () -> adapter.adaptFromJson(Json.createValue(42)));
        assertEquals("Condition must be a boolean or a JSONata expression string "
                + "delimited by {% %}.  Found NUMBER: 42", failure.getMessage());
    }

    @Test
    public void fieldLevelAdapterAppliesBothDirections() throws Exception {
        final Holder literal = new Holder(Condition.of(true));
        final Holder expression = new Holder(Condition.expression("$value >= 20"));

        try (Jsonb jsonb = JsonbBuilder.create()) {
            JsonAsserts.assertJson("{\"Condition\": true}", jsonb.toJson(literal));
            JsonAsserts.assertJson("{\"Condition\": \"{% $value >= 20 %}\"}", jsonb.toJson(expression));
        }

        try (Jsonb jsonb = JsonbBuilder.create()) {
            assertEquals(literal, jsonb.fromJson("{\"Condition\":true}", Holder.class));
            assertEquals(expression, jsonb.fromJson("{\"Condition\":\"{% $value >= 20 %}\"}", Holder.class));
        }
    }

    public record Holder(@JsonbProperty("Condition")
                         @JsonbTypeAdapter(Condition.Adapter.class) Condition condition) {
    }
}
