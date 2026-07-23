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
package org.tomitribe.aws.states.jsonb;

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
