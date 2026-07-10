/*
 * Tomitribe Confidential
 *
 * Copyright Tomitribe Corporation. 2026
 *
 * The source code for this program is not published or otherwise divested
 * of its trade secrets, irrespective of what has been deposited with the
 * U.S. Copyright Office.
 */
package com.tomitribe.aureto.states.test;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonGeneratorFactory;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * JSON comparison utilities in the style of harminie-test's JsonAsserts.
 * Documents are compared with keys sorted and null values excluded, so
 * field order and absent-vs-null differences do not cause failures.
 */
public class JsonAsserts {

    private JsonAsserts() {
    }

    public static void assertJson(final String expected, final String actual) {
        final String e = normalize(expected);
        final String a = normalize(actual);
        assertEquals(e, a);
    }

    /**
     * Asserts a JSON document survives a round trip through the supplied
     * class: deserialized, serialized back, and compared to the original.
     * Fresh Jsonb instances are used for each direction due to caching in
     * Johnzon that hides bugs.
     */
    public static void assertJsonb(final String expectedJson, final Class<?> jsonbClass) throws Exception {
        final Object object;
        try (Jsonb jsonb = JsonbBuilder.create()) {
            object = jsonb.fromJson(expectedJson, jsonbClass);
        }

        final String actual;
        try (Jsonb jsonb = JsonbBuilder.create()) {
            actual = jsonb.toJson(object);
        }

        assertJson(expectedJson, actual);
    }

    /**
     * Asserts an object serializes to the expected JSON, and that the
     * expected JSON survives a round trip through the object's class.
     */
    public static void assertJsonbReadWrite(final Object data, final String expectedJson) throws Exception {
        { // Assert write
            try (Jsonb jsonb = JsonbBuilder.create()) {
                final String actualJson = jsonb.toJson(data);
                assertJson(expectedJson, actualJson);
            }
        }

        { // Assert read
            assertJsonb(expectedJson, data.getClass());
        }
    }

    /**
     * Loads a classpath resource as a UTF-8 string.  Fails with the exact
     * path to create when the resource does not exist.
     */
    public static String resource(final String path) {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException(String.format(
                        "Resource not found.  Add the expected json here: src/test/resources/%s", path));
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (final java.io.IOException e) {
            throw new IllegalStateException(String.format("Unable to read resource: %s", path), e);
        }
    }

    private static String normalize(final String json) {
        final JsonReader reader = Json.createReader(new StringReader(json));

        final JsonValue object = reader.readValue();
        final JsonValue jsonObject = sort(object);

        final Map<String, Object> properties = new HashMap<>(1);
        properties.put(JsonGenerator.PRETTY_PRINTING, true);
        final JsonGeneratorFactory jgf = Json.createGeneratorFactory(properties);
        final StringWriter out = new StringWriter();
        final JsonGenerator jg = jgf.createGenerator(out);
        jg.write(jsonObject);
        jg.flush();

        return out.toString();
    }

    private static JsonValue sort(final JsonValue jsonValue) {
        if (jsonValue.getValueType().equals(JsonValue.ValueType.OBJECT)) {
            return sort(jsonValue.asJsonObject());
        } else if (jsonValue.getValueType().equals(JsonValue.ValueType.ARRAY)) {
            return sort(jsonValue.asJsonArray());
        } else {
            return jsonValue;
        }
    }

    private static JsonArray sort(final JsonArray jsonArray) {
        final JsonArrayBuilder copy = Json.createArrayBuilder();
        for (final JsonValue value : jsonArray) {
            copy.add(sort(value));
        }
        return copy.build();
    }

    private static JsonObject sort(final JsonObject jsonObject) {
        final JsonObjectBuilder copy = Json.createObjectBuilder();

        jsonObject.keySet().stream()
                .sorted()
                .filter(s1 -> !JsonValue.ValueType.NULL.equals(jsonObject.get(s1).getValueType()))
                .forEach(s -> {
                            final JsonValue sorted = sort(jsonObject.get(s));
                            copy.add(s, sorted);
                        }
                );

        return copy.build();
    }
}
