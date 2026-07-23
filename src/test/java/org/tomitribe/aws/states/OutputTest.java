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
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTypeAdapter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Output shares its mechanics with Arguments through the same hidden
 * machinery, so this test covers the type's identity — its own forms, its
 * own adapter, its own error messages — rather than re-proving the shared
 * vocabulary that ArgumentsTest exercises.
 */
class OutputTest {

    @Test
    public void expressionForm() {
        final OutputExpression merge = Output.expression(
                "$merge([$states.input, {'input': $states.result}])");

        assertEquals("$merge([$states.input, {'input': $states.result}])", merge.expression());
        assertEquals("\"{% $merge([$states.input, {'input': $states.result}]) %}\"",
                merge.toJsonValue().toString());
    }

    @Test
    public void objectForm() throws Exception {
        final OutputObject object = Output.builder()
                .expression("customer", "$states.input.customer")
                .expression("resultStatus", "$states.result.status")
                .value("processed", true)
                .build();

        JsonAsserts.assertJson("""
                {
                  "customer": "{% $states.input.customer %}",
                  "resultStatus": "{% $states.result.status %}",
                  "processed": true
                }""", object.toJsonValue().toString());

        assertTrue(object.isExpression("customer"));
        assertEquals("$states.result.status", object.getExpression("resultStatus"));
        assertTrue(object.getBoolean("processed"));
    }

    @Test
    public void messagesNameTheField() {
        final IllegalArgumentException value = assertThrows(IllegalArgumentException.class,
                () -> Output.builder().value("customer", "{% $states.input.customer %}"));
        assertEquals("Value for field \"customer\" looks like a JSONata expression: "
                + "\"{% $states.input.customer %}\".  "
                + "Use expression(\"customer\", \"$states.input.customer\") instead", value.getMessage());

        final IllegalArgumentException union = assertThrows(IllegalArgumentException.class,
                () -> new Output.Adapter().adaptFromJson(jakarta.json.Json.createValue(42)));
        assertEquals("Output must be a JSON object or a JSONata expression string "
                + "delimited by {% %}.  Found NUMBER: 42", union.getMessage());
    }

    @Test
    public void fieldLevelAdapterAppliesBothDirections() throws Exception {
        final Holder expression = new Holder(Output.expression("$states.result"));
        final Holder object = new Holder(Output.builder().value("processed", true).build());

        try (Jsonb jsonb = JsonbBuilder.create()) {
            JsonAsserts.assertJson("{\"Output\": \"{% $states.result %}\"}", jsonb.toJson(expression));
            JsonAsserts.assertJson("{\"Output\": {\"processed\": true}}", jsonb.toJson(object));
        }

        try (Jsonb jsonb = JsonbBuilder.create()) {
            assertEquals(expression, jsonb.fromJson("{\"Output\":\"{% $states.result %}\"}", Holder.class));
            assertEquals(object, jsonb.fromJson("{\"Output\":{\"processed\":true}}", Holder.class));
        }
    }

    public record Holder(@JsonbProperty("Output")
                         @JsonbTypeAdapter(Output.Adapter.class) Output output) {
    }
}
