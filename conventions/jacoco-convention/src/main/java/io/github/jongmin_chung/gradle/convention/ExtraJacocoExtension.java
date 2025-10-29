package io.github.jongmin_chung.gradle.convention;

import javax.inject.Inject;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.jspecify.annotations.NonNull;

public abstract class ExtraJacocoExtension {

    static final String EXTENSION_NAME = "jacocoExt";
    static final boolean DEFAULT_ENABLED = true;

    final Property<@NonNull Boolean> enabled;

    @Inject
    protected ExtraJacocoExtension(ObjectFactory objects) {
        this.enabled = objects.property(Boolean.class).convention(DEFAULT_ENABLED);
    }
}
