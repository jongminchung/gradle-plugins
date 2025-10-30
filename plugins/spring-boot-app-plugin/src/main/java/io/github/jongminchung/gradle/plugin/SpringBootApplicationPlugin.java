package io.github.jongminchung.gradle.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.testing.jacoco.plugins.JacocoPlugin;
import org.gradle.testing.jacoco.plugins.JacocoReportAggregationPlugin;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.gradle.plugin.SpringBootPlugin;


import io.freefair.gradle.plugins.lombok.LombokPlugin;
import io.spring.gradle.dependencymanagement.DependencyManagementPlugin;

import io.github.jongminchung.gradle.convention.*;

@SuppressWarnings("UnstableApiUsage")
public class SpringBootApplicationPlugin implements Plugin<@NonNull Project> {
    @Override
    public void apply(Project target) {
        var plugins = target.getPluginManager();

        plugins.apply(JavaPlugin.class);
        plugins.apply(JavaConventionPlugin.class);

        plugins.apply(SpringBootPlugin.class);
        plugins.apply(SpringBootConventionPlugin.class);

        plugins.apply(DependencyManagementPlugin.class);

        plugins.apply(JacocoReportAggregationPlugin.class);
        plugins.apply(JacocoReportAggregationConventionPlugin.class);
        plugins.apply(JacocoPlugin.class);
        plugins.apply(JacocoConventionPlugin.class);

        plugins.apply(MavenPublishPlugin.class);
        plugins.apply(PublishMavenConventionPlugin.class);

        plugins.apply("jvm-test-suite");
        plugins.apply(JvmTestSuiteConventionPlugin.class);

        plugins.apply(ErrorproneConventionPlugin.class);

        plugins.apply(LombokPlugin.class);
    }
}
