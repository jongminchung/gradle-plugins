package io.github.jongmin_chung.gradle.convention;

import java.util.List;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.compile.JavaCompile;
import org.jspecify.annotations.NonNull;

public class NullawayConventionPlugin implements Plugin<@NonNull Project> {
    @Override
    public void apply(Project target) {
        target.afterEvaluate(project -> {
            if (!project.getPlugins().hasPlugin(JavaPlugin.class)) {
                project.getLogger().warn("""
                                [NullawayConventionPlugin] The 'java' plugin is not applied to the project.

                                NullawayConventionPlugin requires the Java plugin to be applied.
                                """);
                return;
            }

            var dependencies = project.getDependencies();
            dependencies.add(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME, "org.jspecify:jspecify:1.0.0");
            dependencies.add("errorprone", "com.uber.nullaway:nullaway:0.12.10");

            project.getTasks().withType(JavaCompile.class).configureEach(javaCompile -> {
                var compileArgs = List.of(
                        "-Xplugin:ErrorProne",
                        "-Xep:NullAway:ERROR",
                        "-XepOpt:NullAway:AnnotatedPackages=" + project.getGroup());

                javaCompile.getOptions().getCompilerArgs().addAll(compileArgs);
            });
        });
    }
}
