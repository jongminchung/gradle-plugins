package io.github.jongminchung.gradle.convention;

import static org.assertj.core.api.Assertions.assertThat;

import org.gradle.api.Project;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.publish.Publication;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

class PublishMavenConventionPluginTest {

    @Test
    void registersExtensionWithDefaultEnabled() {
        Project project = ProjectBuilder.builder().build();

        project.getPluginManager().apply(PublishMavenConventionPlugin.class);

        ExtraMavenPublishExtension extension =
                project.getExtensions().findByType(ExtraMavenPublishExtension.class);
        assertThat(extension).isNotNull();
        assertThat(extension.getEnabled().get()).isTrue();
    }

    @Test
    void enablesBuildIdentifierOnPublications() {
        Project project = ProjectBuilder.builder().build();

        project.getPluginManager().apply("maven-publish");
        project.getPluginManager().apply(PublishMavenConventionPlugin.class);

        PublishingExtension publishing = project.getExtensions().getByType(PublishingExtension.class);
        RecordingPublication publication = new RecordingPublication("recording");
        publishing.getPublications().add(publication);

        evaluate(project);

        assertThat(publication.withBuildIdentifierCalled).isTrue();
    }

    private static void evaluate(Project project) {
        ((ProjectInternal) project).evaluate();
    }

    private static final class RecordingPublication implements Publication {
        private final String name;
        private boolean withBuildIdentifierCalled;

        private RecordingPublication(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void withoutBuildIdentifier() {
            this.withBuildIdentifierCalled = false;
        }

        @Override
        public void withBuildIdentifier() {
            this.withBuildIdentifierCalled = true;
        }
    }
}
