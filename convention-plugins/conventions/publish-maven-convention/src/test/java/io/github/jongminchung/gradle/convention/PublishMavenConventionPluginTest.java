package io.github.jongminchung.gradle.convention;

import static org.assertj.core.api.Assertions.assertThat;

import org.gradle.api.Project;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.internal.PublicationInternal;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

class PublishMavenConventionPluginTest {

    @Test
    void registersExtensionWithDefaultEnabled() {
        Project project = ProjectBuilder.builder().build();

        project.getPluginManager().apply(PublishMavenConventionPlugin.class);

        ExtraMavenPublishExtension extension = project.getExtensions().findByType(ExtraMavenPublishExtension.class);
        assertThat(extension).isNotNull();
        assertThat(extension.getEnabled().get()).isTrue();
    }

    @Test
    void enablesBuildIdentifierOnPublications() {
        Project project = ProjectBuilder.builder().build();

        project.getPluginManager().apply("maven-publish");
        project.getPluginManager().apply(PublishMavenConventionPlugin.class);

        PublishingExtension publishing = project.getExtensions().getByType(PublishingExtension.class);
        MavenPublication publication = publishing.getPublications().create("recording", MavenPublication.class);

        evaluate(project);

        PublicationInternal<?> internalPublication = (PublicationInternal<?>) publication;
        assertThat(internalPublication.isPublishBuildId()).isTrue();
    }

    private static void evaluate(Project project) {
        ((ProjectInternal) project).evaluate();
    }
}
