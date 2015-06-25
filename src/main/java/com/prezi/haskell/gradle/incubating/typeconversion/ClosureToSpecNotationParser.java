/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.prezi.haskell.gradle.incubating.typeconversion;

import groovy.lang.Closure;
import org.gradle.api.specs.Spec;
import org.gradle.api.specs.Specs;

import java.util.Collection;

public class ClosureToSpecNotationParser<T> implements NotationConverter<Closure, Spec<T>> {
    private final Class<T> type;

    public ClosureToSpecNotationParser(Class<T> type) {
        this.type = type;
    }

    public void convert(Closure notation, NotationConvertResult<? super Spec<T>> result) throws TypeConversionException {
        Spec<T> spec = Specs.convertClosureToSpec(notation);
        result.converted(spec);
    }

    public void describe(Collection<String> candidateFormats) {
        candidateFormats.add(String.format("Closure that returns boolean and takes a single %s as a parameter.", type.getSimpleName()));
    }
}
