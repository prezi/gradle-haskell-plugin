/*
 * Copyright 2011 the original author or authors.
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

import java.util.Collection;

public abstract class TypedNotationParser<N, T> implements NotationParser<Object, T> {

    private final Class<N> typeToken;

    public TypedNotationParser(Class<N> typeToken) {
        assert typeToken != null : "typeToken cannot be null";
        this.typeToken = typeToken;
    }

    public TypedNotationParser(TypeInfo<N> typeToken) {
        assert typeToken != null : "typeToken cannot be null";
        this.typeToken = typeToken.getTargetType();
    }

    public void describe(Collection<String> candidateFormats) {
        candidateFormats.add(String.format("Instances of %s.", typeToken.getSimpleName()));
    }

    public T parseNotation(Object notation) {
        if (!typeToken.isInstance(notation)) {
            throw new UnsupportedNotationException(notation);
        }
        return parseType(typeToken.cast(notation));
    }

    abstract protected T parseType(N notation);
}
