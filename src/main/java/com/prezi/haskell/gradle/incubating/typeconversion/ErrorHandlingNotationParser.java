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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class ErrorHandlingNotationParser<N, T> implements NotationParser<N, T> {
    private final String targetTypeDisplayName;
    private final String invalidNotationMessage;
    private final boolean allowNullInput;
    private final NotationParser<N, T> delegate;

    public ErrorHandlingNotationParser(String targetTypeDisplayName, String invalidNotationMessage, boolean allowNullInput, NotationParser<N, T> delegate) {
        this.targetTypeDisplayName = targetTypeDisplayName;
        this.invalidNotationMessage = invalidNotationMessage;
        this.allowNullInput = allowNullInput;
        this.delegate = delegate;
    }

    public void describe(Collection<String> candidateFormats) {
        delegate.describe(candidateFormats);
    }

    public T parseNotation(N notation) {
        String failure;
        if (notation == null && !allowNullInput) {
            failure = String.format("Cannot convert a null value to %s.", targetTypeDisplayName);
        } else {
            try {
                return delegate.parseNotation(notation);
            } catch (UnsupportedNotationException e) {
                failure = String.format("Cannot convert the provided notation to %s: %s.", targetTypeDisplayName, e.getNotation());
            }
        }

        List<String> formats = new ArrayList<String>();
        describe(formats);

        throw new UnsupportedNotationException(notation, failure, invalidNotationMessage, formats);
    }
}
