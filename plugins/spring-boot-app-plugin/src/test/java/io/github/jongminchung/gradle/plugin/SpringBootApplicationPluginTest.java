package io.github.jongminchung.gradle.plugin;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.jongminchung.gradle.convention.ErrorproneConventionPlugin;
import io.github.jongminchung.gradle.convention.JacocoConventionPlugin;
import io.github.jongminchung.gradle.convention.JacocoReportAggregationConventionPlugin;
import io.github.jongminchung.gradle.convention.JavaConventionPlugin;
import io.github.jongminchung.gradle.convention.JvmTestSuiteConventionPlugin;
import io.github.jongminchung.gradle.convention.PublishMavenConventionPlugin;
import io.github.jongminchung.gradle.convention.SpringBootConventionPlugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.testing.jacoco.plugins.JacocoPlugin;
import org.gradle.testing.jacoco.plugins.JacocoReportAggregationPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.gradle.plugin.SpringBootPlugin;

class SpringBootApplicationPluginTest {

    @Test
    void appliesAllRequiredPlugins() {
        Project project = ProjectBuilder.builder().build();

        project.getPluginManager().apply(SpringBootApplicationPlugin.class);

        assertThat(project.getPlugins().hasPlugin(JavaPlugin.class)).isTrue();
        assertThat(project.getPlugins().hasPlugin(JavaConventionPlugin.class)).isTrue();
        assertThat(project.getPlugins().hasPlugin(SpringBootPlugin.class)).isTrue();
        assertThat(project.getPlugins().hasPlugin(SpringBootConventionPlugin.class)).isTrue();
        assertThat(project.getPlugins().hasPlugin(MavenPublishPlugin.class)).isTrue();
        assertThat(project.getPlugins().hasPlugin(PublishMavenConventionPlugin.class)).isTrue();
        assertThat(project.getPlugins().hasPlugin(JacocoPlugin.class)).isTrue();
        assertThat(project.getPlugins().hasPlugin(JacocoConventionPlugin.class)).isTrue();
        assertThat(project.getPlugins().hasPlugin(JacocoReportAggregationPlugin.class)).isTrue();
        assertThat(project.getPlugins().hasPlugin(JacocoReportAggregationConventionPlugin.class)).isTrue();
        assertThat(project.getPlugins().hasPlugin("jvm-test-suite")).isTrue();
        assertThat(project.getPlugins().hasPlugin(JvmTestSuiteConventionPlugin.class)).isTrue();
        assertThat(project.getPlugins().hasPlugin(ErrorproneConventionPlugin.class)).isTrue();
    }
}
