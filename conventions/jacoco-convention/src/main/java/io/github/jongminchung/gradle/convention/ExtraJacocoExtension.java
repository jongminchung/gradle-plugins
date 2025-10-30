package io.github.jongminchung.gradle.convention;

import javax.inject.Inject;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.jspecify.annotations.NonNull;

public abstract class ExtraJacocoExtension {
    static final String EXTENSION_NAME = "jacocoExt";

    private final Property<@NonNull Boolean> enabled;

    @Inject
    public ExtraJacocoExtension(ObjectFactory objects) {
        this.enabled = objects.property(Boolean.class).convention(true);
    }

    public Property<@NonNull Boolean> getEnabled() {
        return enabled;
    }
}
