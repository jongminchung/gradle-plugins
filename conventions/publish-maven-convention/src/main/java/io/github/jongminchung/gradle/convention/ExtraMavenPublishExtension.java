package io.github.jongminchung.gradle.convention;

import javax.inject.Inject;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.jspecify.annotations.NonNull;

public abstract class ExtraMavenPublishExtension {
    public static final String EXTRA_MAVEN_PUBLISH_EXTENSION_NAME = "mavenPublishExt";

    private final Property<@NonNull Boolean> enabled;

    @Inject
    public ExtraMavenPublishExtension(ObjectFactory objects) {
        this.enabled = objects.property(Boolean.class).convention(true);
    }

    public Property<@NonNull Boolean> getEnabled() {
        return enabled;
    }
}
