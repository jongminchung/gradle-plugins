package io.github.jongminchung.gradle.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testing.jacoco.plugins.JacocoPlugin;
import org.gradle.testing.jacoco.plugins.JacocoReportAggregationPlugin;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.gradle.plugin.SpringBootPlugin;
import org.springframework.boot.gradle.tasks.bundling.BootJar;

import io.github.jongminchung.gradle.convention.*;

import net.ltgt.gradle.errorprone.ErrorPronePlugin;

import io.freefair.gradle.plugins.lombok.LombokPlugin;
import io.spring.gradle.dependencymanagement.DependencyManagementPlugin;

@SuppressWarnings("UnstableApiUsage")
public class SpringBootApplicationPlugin implements Plugin<@NonNull Project> {
    static final String SPRING_BOOT_JAR_NAME = "app.jar";

    @Override
    public void apply(Project target) {
        var plugins = target.getPluginManager();

        plugins.apply(JavaPlugin.class);
        plugins.apply(JavaConventionPlugin.class);

        plugins.apply(SpringBootPlugin.class);
        plugins.apply(SpringBootConventionPlugin.class);

        target.getTasks().named("bootJar", BootJar.class, bootJar -> bootJar.getArchiveFileName()
                .set(SPRING_BOOT_JAR_NAME));

        plugins.apply(DependencyManagementPlugin.class);

        plugins.apply(JacocoPlugin.class);
        plugins.apply(JacocoConventionPlugin.class);

        plugins.apply(JacocoReportAggregationPlugin.class);
        plugins.apply(JacocoReportAggregationConventionPlugin.class);

        plugins.apply(JvmTestSuiteConventionPlugin.class);

        plugins.apply(ErrorPronePlugin.class);
        plugins.apply(ErrorProneConventionPlugin.class);
        plugins.apply(SpotlessConventionPlugin.class);

        plugins.apply(LombokPlugin.class);
    }
}
