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
        target.afterEvaluate(project -> {
            var pluginManager = project.getPluginManager();

            pluginManager.withPlugin("jacoco-report-aggregation", appliedPlugin -> {
                var deps = project.getDependencies();

                project.getAllprojects().stream()
                        .filter(p -> p.getPluginManager().hasPlugin("jacoco"))
                        .filter(p -> p != project)
                        .forEach(p -> deps.add(
                                JACOCO_AGGREGATION_CONFIGURATION_NAME, deps.project(Map.of("path", p.getPath()))));
            });
        });
    }
}
