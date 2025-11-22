package io.github.jongminchung.gradle.plugin;

import org.gradle.api.file.RegularFileProperty;

public interface OpenApiSpringExtension {
    RegularFileProperty getInputFile();
    RegularFileProperty getOutputFile();
}