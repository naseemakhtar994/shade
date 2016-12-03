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
package io.t28.shade.compiler;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.inject.Inject;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import io.t28.shade.annotation.Preferences;
import io.t28.shade.compiler.factory.TypeFactory;
import io.t28.shade.compiler.inject.EditorModule;
import io.t28.shade.compiler.inject.EntityModule;
import io.t28.shade.compiler.inject.PreferencesModule;
import io.t28.shade.compiler.inject.ShadeModule;
import io.t28.shade.compiler.utils.Writer;

@SuppressWarnings({"unused", "WeakerAccess"})
@AutoService(Processor.class)
public class ShadeProcessor extends AbstractProcessor {
    private static final Key<String> PACKAGE_NAME_KEY = Key.get(String.class, Names.named("PackageName"));
    private static final Key<TypeFactory> TYPE_FACTORY_KEY = Key.get(TypeFactory.class, Names.named("Preferences"));

    private Injector injector;

    @Inject
    private Messager messager;

    @Inject
    private Writer writer;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return ImmutableSet.of(Preferences.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(@Nonnull ProcessingEnvironment environment) {
        super.init(environment);
        injector = Guice.createInjector(new ShadeModule(environment));
    }

    @Override
    public boolean process(@Nonnull Set<? extends TypeElement> annotations, @Nonnull RoundEnvironment environment) {
        injector.injectMembers(this);
        environment.getElementsAnnotatedWith(Preferences.class)
                .stream()
                .map(TypeElement.class::cast)
                .filter(element -> {
                    final ElementKind kind = element.getKind();
                    if (kind == ElementKind.INTERFACE || kind == ElementKind.CLASS) {
                        return true;
                    }
                    messager.printMessage(Diagnostic.Kind.ERROR, "@Preferences is not allowed to use for " + kind);
                    return false;
                })
                .forEach(element -> {
                    try {
                        final Injector childInjector = injector.createChildInjector(
                                new PreferencesModule(element),
                                new EntityModule(),
                                new EditorModule()
                        );
                        final String packageName = childInjector.getInstance(PACKAGE_NAME_KEY);
                        final TypeFactory factory = childInjector.getInstance(TYPE_FACTORY_KEY);
                        writer.write(packageName, factory.create());
                    } catch (Exception e) {
                        messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
                    }
                });
        return true;
    }
}
