import javax.inject.Inject;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.jspecify.annotations.NonNull;

public class ExtraJavaExtension {
    static final String EXTENSION_NAME = "javaExt";

    private final Property<@NonNull Boolean> enabled;
    private final Property<@NonNull Boolean> withJavadocJar;
    private final Property<@NonNull Boolean> withSourcesJar;

    @Inject
    public ExtraJavaExtension(ObjectFactory objects) {
        this.enabled = objects.property(Boolean.class).convention(true);
        this.withJavadocJar = objects.property(Boolean.class).convention(true);
        this.withSourcesJar = objects.property(Boolean.class).convention(true);
    }

    public Property<@NonNull Boolean> getEnabled() {
        return enabled;
    }

    public Property<@NonNull Boolean> getWithJavadocJar() {
        return withJavadocJar;
    }

    public Property<@NonNull Boolean> getWithSourcesJar() {
        return withSourcesJar;
    }
}
