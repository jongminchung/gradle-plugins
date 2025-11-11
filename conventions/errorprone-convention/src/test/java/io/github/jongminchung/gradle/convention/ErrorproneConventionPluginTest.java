package io.github.jongminchung.gradle.convention;

import static org.assertj.core.api.Assertions.assertThat;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import net.ltgt.gradle.errorprone.ErrorPronePlugin;
import net.ltgt.gradle.nullaway.NullAwayPlugin;

class ErrorproneConventionPluginTest {

    @Test
    void appliesErrorproneStackWhenJavaPluginPresent() {
        Project project = ProjectBuilder.builder().build();

        project.getPluginManager().apply(ErrorproneConventionPlugin.class);
        project.getPluginManager().apply(JavaPlugin.class);

        assertThat(project.getPlugins().hasPlugin(ErrorPronePlugin.class)).isTrue();
        assertThat(project.getPlugins().hasPlugin(NullAwayPlugin.class)).isTrue();
    }

    @Test
    void wiresExpectedDependencies() {
        Project project = ProjectBuilder.builder().build();

        project.getPluginManager().apply(ErrorproneConventionPlugin.class);
        project.getPluginManager().apply(JavaPlugin.class);

        Configuration implementation =
                project.getConfigurations().getByName(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME);
        assertThat(implementation.getDependencies())
                .anyMatch(dep -> "org.jspecify".equals(dep.getGroup()) && "jspecify".equals(dep.getName()));

        Configuration errorprone = project.getConfigurations().getByName(ErrorPronePlugin.CONFIGURATION_NAME);
        assertThat(errorprone.getDependencies())
                .anyMatch(dep -> "com.uber.nullaway".equals(dep.getGroup()) && "nullaway".equals(dep.getName()));
        assertThat(errorprone.getDependencies())
                .anyMatch(dep ->
                        "com.google.errorprone".equals(dep.getGroup()) && "error_prone_core".equals(dep.getName()));
    }
}
