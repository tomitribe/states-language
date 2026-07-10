/*
 * Tomitribe Confidential
 *
 * Copyright Tomitribe Corporation. 2026
 *
 * The source code for this program is not published or otherwise divested
 * of its trade secrets, irrespective of what has been deposited with the
 * U.S. Copyright Office.
 */
package com.tomitribe.aureto.states.jsonb;

import jakarta.json.bind.config.PropertyVisibilityStrategy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Standard method-based JSON-B visibility, except Lombok's generated
 * {@code toBuilder()} is not a property.  Without this, Johnzon (through
 * 2.1.0) treats any public no-arg method on a record as a property and
 * serializes a bogus "toBuilder" field.  The proper JSON-B remedy is
 * {@code @JsonbTransient}, but Lombok cannot annotate the methods it
 * generates.
 *
 * This class and the {@code @JsonbVisibility} annotation in
 * package-info.java can be deleted once the project moves to a Johnzon
 * release that limits record property discovery to record components.
 */
public class ModelVisibility implements PropertyVisibilityStrategy {

    @Override
    public boolean isVisible(final Field field) {
        return false;
    }

    @Override
    public boolean isVisible(final Method method) {
        return !"toBuilder".equals(method.getName());
    }
}
