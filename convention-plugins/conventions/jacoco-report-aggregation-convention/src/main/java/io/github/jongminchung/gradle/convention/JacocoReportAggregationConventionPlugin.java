package io.github.jongminchung.gradle.convention;

import static org.gradle.testing.jacoco.plugins.JacocoReportAggregationPlugin.JACOCO_AGGREGATION_CONFIGURATION_NAME;

import java.util.Map;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class JacocoReportAggregationConventionPlugin implements Plugin<@NotNull Project> {
    @Override
    public void apply(Project target) {
        target.getGradle().projectsEvaluated(gradle -> {
            var pluginManager = target.getPluginManager();

            pluginManager.withPlugin("jacoco-report-aggregation", appliedPlugin -> {
                var deps = target.getDependencies();

                target.getAllprojects().stream()
                        .filter(project -> project.getPluginManager().hasPlugin("jacoco"))
                        .filter(project -> project != target)
                        .forEach(project -> deps.add(
                                JACOCO_AGGREGATION_CONFIGURATION_NAME,
                                deps.project(Map.of("path", project.getPath()))));
            });
        });
    }
}
