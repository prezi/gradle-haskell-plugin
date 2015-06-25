package com.prezi.haskell.gradle.incubating;

import org.gradle.api.Nullable;

import java.util.List;

public interface BinaryNamingScheme {
	String getLifecycleTaskName();

    String getTaskName(@Nullable String verb);

    String getTaskName(@Nullable String verb, @Nullable String target);

    String getOutputDirectoryBase();

    String getDescription();

    List<String> getVariantDimensions();
}
