/*
 * Tomitribe Confidential
 *
 * Copyright Tomitribe Corporation. 2026
 *
 * The source code for this program is not published or otherwise divested
 * of its trade secrets, irrespective of what has been deposited with the
 * U.S. Copyright Office.
 */
package com.tomitribe.aureto.states;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
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
            if (!List.class.isAssignableFrom(field.getType())) continue;

            final String name = field.getName();
            final String clearMethod = "clear" + Character.toUpperCase(name.charAt(0)) + name.substring(1);

            assertTrue(builderMethods.contains(clearMethod), String.format(
                    "List field '%s' in class %s should be annotated @Singular.  Builder is missing %s()",
                    name, clazz.getName(), clearMethod
            ));
        }
    }
}
