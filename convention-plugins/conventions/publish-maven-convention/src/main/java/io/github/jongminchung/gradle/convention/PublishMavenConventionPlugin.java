package io.github.jongminchung.gradle.convention;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.publish.Publication;
import org.gradle.api.publish.PublishingExtension;
import org.jspecify.annotations.NonNull;

public class PublishMavenConventionPlugin implements Plugin<@NonNull Project> {
    @Override
    public void apply(Project target) {
        var extraMavenPublishExtension = target.getExtensions()
                .create(
                        ExtraMavenPublishExtension.EXTRA_MAVEN_PUBLISH_EXTENSION_NAME,
                        ExtraMavenPublishExtension.class);

        target.getPluginManager().withPlugin("maven-publish", plugin -> {
            if (!extraMavenPublishExtension.getEnabled().getOrElse(true)) {
                return;
            }

            var publishing = target.getExtensions().findByType(PublishingExtension.class);

            if (publishing != null) {
                var publications = publishing.getPublications();

                publications.all(Publication::withBuildIdentifier);
            }
        });
    }
}
