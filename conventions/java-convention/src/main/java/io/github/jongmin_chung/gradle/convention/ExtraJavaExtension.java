package io.github.jongmin_chung.gradle.convention;

import javax.inject.Inject;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.jspecify.annotations.NonNull;

public abstract class ExtraJavaExtension {
    static final String EXTENSION_NAME = "javaExt";

    final Property<@NonNull Boolean> enabled;
    final Property<@NonNull Boolean> withJavadocJar;
    final Property<@NonNull Boolean> withSourcesJar;

    @Inject
    protected ExtraJavaExtension(ObjectFactory objects) {
        this.enabled = objects.property(Boolean.class).convention(true);

        this.withJavadocJar = objects.property(Boolean.class).convention(true);
        this.withSourcesJar = objects.property(Boolean.class).convention(true);
    }
}
