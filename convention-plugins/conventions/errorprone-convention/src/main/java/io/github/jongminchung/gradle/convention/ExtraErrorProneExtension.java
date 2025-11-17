package io.github.jongminchung.gradle.convention;

import javax.inject.Inject;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.jspecify.annotations.NonNull;

public abstract class ExtraErrorProneExtension {
    static final String EXTENSION_NAME = "errorproneExt";

    static final String ERROR_PRONE_VERSION = "2.44.0";
    static final String NULL_AWAY_VERSION = "0.12.12";

    private final Property<@NonNull String> errorProneVersion;
    private final Property<@NonNull String> nullAwayVersion;

    @Inject
    public ExtraErrorProneExtension(ObjectFactory objects) {
        this.errorProneVersion = objects.property(String.class).convention(ERROR_PRONE_VERSION);
        this.nullAwayVersion = objects.property(String.class).convention(NULL_AWAY_VERSION);
    }

    public Property<@NonNull String> getErrorProneVersion() {
        return errorProneVersion;
    }

    public Property<@NonNull String> getNullAwayVersion() {
        return nullAwayVersion;
    }
}
