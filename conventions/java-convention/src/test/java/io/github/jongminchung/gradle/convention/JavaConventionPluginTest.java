package io.github.jongminchung.gradle.convention;

import static org.assertj.core.api.Assertions.assertThat;

import org.gradle.api.Project;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

class JavaConventionPluginTest {

    @Test
    void createsExtraJavaExtensionWithDefaults() {
        Project project = ProjectBuilder.builder().build();

        project.getPluginManager().apply(JavaConventionPlugin.class);

        ExtraJavaExtension extension = project.getExtensions().findByType(ExtraJavaExtension.class);
        assertThat(extension).isNotNull();
        assertThat(extension.getEnabled().get()).isTrue();
        assertThat(extension.getWithJavadocJar().get()).isTrue();
        assertThat(extension.getWithSourcesJar().get()).isTrue();
    }

    @Test
    void registersJavadocAndSourcesTasksWhenJavaPluginPresent() {
        Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply(JavaPlugin.class);
        project.getPluginManager().apply(JavaConventionPlugin.class);

        evaluate(project);

        assertThat(project.getTasks().findByName("javadocJar")).isNotNull();
        assertThat(project.getTasks().findByName("sourcesJar")).isNotNull();
    }

    private static void evaluate(Project project) {
        ((ProjectInternal) project).evaluate();
    }
}
