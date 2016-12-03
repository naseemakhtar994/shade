/*
 * Copyright (c) 2016 Tatsuya Maki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.t28.shade.compiler.metadata;

import com.google.common.base.Strings;

import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

import io.t28.shade.annotation.Preferences;
import io.t28.shade.annotation.Property;
import io.t28.shade.compiler.utils.TypeElements;

import static java.util.stream.Collectors.toList;

public class PreferencesMetadata {
    private final TypeElement element;
    private final Preferences annotation;
    private final Elements elementUtils;

    @Inject
    public PreferencesMetadata(@Nonnull TypeElement element,
                               @Nonnull Preferences annotation,
                               @Nonnull Elements elementUtils) {
        final Set<Modifier> modifiers = element.getModifiers();
        if (!modifiers.contains(Modifier.ABSTRACT)) {
            throw new IllegalArgumentException("Annotated class(" + element.getSimpleName() + ") with @Preferences must be an abstract class or interface");
        }
        checkConstructor(element);

        this.element = element;
        this.annotation = annotation;
        this.elementUtils = elementUtils;
    }

    public boolean isDefault() {
        return Strings.isNullOrEmpty(annotation.name());
    }

    @Nonnull
    public String getName() {
        return annotation.name();
    }

    public int getMode() {
        return annotation.mode();
    }

    @Nonnull
    public List<PropertyMetadata> getProperties() {
        return element.getEnclosedElements()
                .stream()
                .filter(enclosed -> {
                    final Property annotation = enclosed.getAnnotation(Property.class);
                    return annotation != null;
                })
                .map(enclosed -> {
                    final ExecutableElement executable = (ExecutableElement) enclosed;
                    return new PropertyMetadata(executable, elementUtils);
                })
                .collect(toList());
    }

    private static void checkConstructor(TypeElement element) {
        final List<ExecutableElement> constructors = TypeElements.findConstructors(element);
        if (constructors.isEmpty()) {
            return;
        }

        final boolean isMatched = constructors.stream()
                .anyMatch(constructor -> {
                    final Set<Modifier> modifiers = constructor.getModifiers();
                    if (modifiers.contains(Modifier.PRIVATE) || modifiers.contains(Modifier.FINAL)) {
                        return false;
                    }

                    final List<? extends VariableElement> parameters = constructor.getParameters();
                    return parameters.isEmpty();
                });
        if (isMatched) {
            return;
        }
        throw new IllegalArgumentException("Annotated class(" + element.getSimpleName() + ") with @Preferences must provide an overridable empty constructor");
    }
}
