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

import jakarta.json.bind.annotation.JsonbTypeAdapter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Sweeps every class in the model and enforces the structural conventions:
 * immutable records built exclusively through Lombok builders, builder
 * classes named Foo.Builder, and List fields annotated with @Singular so
 * entries can be added one at a time.
 */
public class VerifyConventionsTest {

    static Stream<Class<?>> classes() {
        final Reflections reflections = new Reflections(
                VerifyConventionsTest.class.getPackageName(),
                new SubTypesScanner(false) // include Object.class too
        );
        return Stream.concat(
                        reflections.getSubTypesOf(Object.class).stream(),
                        reflections.getSubTypesOf(Record.class).stream()
                ).distinct()
                .filter(aClass -> aClass.getPackageName().equals(VerifyConventionsTest.class.getPackageName()))
                .filter(aClass -> Modifier.isPublic(aClass.getModifiers())) // conventions govern the public model
                .filter(aClass -> !aClass.isMemberClass())
                .filter(aClass -> !aClass.isInterface())
                .filter(aClass -> !aClass.getName().endsWith("Test"))
                ;
    }

    /**
     * Has a public static builder method
     */
    @ParameterizedTest(name = "hasBuilder - {0}")
    @MethodSource("classes")
    public void hasBuilder(final Class<?> clazz) throws Exception {
        final Method builder = clazz.getMethod("builder");
        assertNotNull(builder, clazz.getSimpleName() + " should have builder() method");
        assertTrue(Modifier.isPublic(builder.getModifiers()), "builder() should be public");
        assertTrue(Modifier.isStatic(builder.getModifiers()), "builder() should be static");
    }

    /**
     * Has a toBuilder method
     */
    @ParameterizedTest(name = "hasToBuilder - {0}")
    @MethodSource("classes")
    public void hasToBuilder(final Class<?> clazz) throws Exception {
        assertNotNull(clazz.getMethod("toBuilder"), clazz.getSimpleName() + " should have toBuilder() method");
    }

    /**
     * All fields are declared final
     */
    @ParameterizedTest(name = "immutable - {0}")
    @MethodSource("classes")
    public void immutable(final Class<?> clazz) throws Exception {
        final Field[] fields = clazz.getDeclaredFields();

        for (final Field field : fields) {
            final boolean isFinal = Modifier.isFinal(field.getModifiers());

            assertTrue(isFinal, String.format(
                    "Field '%s' in class %s is not final",
                    field.getName(), clazz.getName()
            ));
        }
    }

    /**
     * All model classes are records
     */
    @ParameterizedTest(name = "isRecord - {0}")
    @MethodSource("classes")
    public void isRecord(final Class<?> clazz) throws Exception {
        assertTrue(clazz.isRecord(), clazz.getSimpleName() + " should be a record");
    }

    /**
     * Builder class is a nested class named Foo.Builder
     */
    @ParameterizedTest(name = "builderIsNamedBuilder - {0}")
    @MethodSource("classes")
    public void builderIsNamedBuilder(final Class<?> clazz) throws Exception {
        final Class<?> builderClass = clazz.getMethod("builder").getReturnType();
        assertEquals("Builder", builderClass.getSimpleName(), String.format(
                "Builder for %s should be named Builder.  Use @Builder(builderClassName = \"Builder\")",
                clazz.getSimpleName()
        ));
        assertEquals(clazz, builderClass.getEnclosingClass(), String.format(
                "Builder for %s should be a nested class of %s",
                clazz.getSimpleName(), clazz.getSimpleName()
        ));
    }

    /**
     * Every field of an adapted type carries its field-level
     * @JsonbTypeAdapter.  Johnzon 2.1.0 honors class-level adapters when
     * serializing but ignores them when deserializing a record field, so a
     * state missing this annotation round-trips asymmetrically: writes
     * fine, fails to read.
     */
    @ParameterizedTest(name = "adaptedFieldsCarryAdapter - {0}")
    @MethodSource("classes")
    public void adaptedFieldsCarryAdapter(final Class<?> clazz) throws Exception {
        final Map<Class<?>, Class<?>> adapters = Map.of(
                Assign.class, Assign.Adapter.class,
                Arguments.class, Arguments.Adapter.class,
                Output.class, Output.Adapter.class,
                Items.class, Items.Adapter.class,
                ItemSelector.class, ItemSelector.Adapter.class,
                BatchInput.class, BatchInput.Adapter.class,
                Condition.class, Condition.Adapter.class
        );

        for (final Field field : clazz.getDeclaredFields()) {
            final Class<?> adapterClass = adapters.get(field.getType());
            if (adapterClass == null) continue;

            final JsonbTypeAdapter adapter = field.getAnnotation(JsonbTypeAdapter.class);
            assertNotNull(adapter, String.format(
                    "%s field '%s' in class %s must be annotated"
                            + " @JsonbTypeAdapter(%s.class): Johnzon ignores"
                            + " class-level adapters when deserializing record fields",
                    field.getType().getSimpleName(), field.getName(), clazz.getName(),
                    adapterClass.getName()));
            assertEquals(adapterClass, adapter.value(), String.format(
                    "%s field '%s' in class %s uses the wrong adapter",
                    field.getType().getSimpleName(), field.getName(), clazz.getName()));
        }
    }

    /**
     * All List fields allow @Singular so entries can be added one at a time
     */
    @ParameterizedTest(name = "listsUseSingular - {0}")
    @MethodSource("classes")
    public void listsUseSingular(final Class<?> clazz) throws Exception {
        final Class<?> builderClass = clazz.getMethod("builder").getReturnType();
        final Set<String> builderMethods = Arrays.stream(builderClass.getMethods())
                .map(Method::getName)
                .collect(Collectors.toSet());

        for (final Field field : clazz.getDeclaredFields()) {
            // declared as List exactly; JsonArray fields are adapted types, not @Singular candidates
            if (!List.class.equals(field.getType())) continue;

            final String name = field.getName();
            final String clearMethod = "clear" + Character.toUpperCase(name.charAt(0)) + name.substring(1);

            assertTrue(builderMethods.contains(clearMethod), String.format(
                    "List field '%s' in class %s should be annotated @Singular.  Builder is missing %s()",
                    name, clazz.getName(), clearMethod
            ));
        }
    }
}
