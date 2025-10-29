package io.github.jongmin_chung.gradle.convention;

import java.util.List;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.compile.JavaCompile;
import org.jspecify.annotations.NonNull;

public class ErrorproneConventionPlugin implements Plugin<@NonNull Project> {
    @Override
    public void apply(Project target) {
        target.afterEvaluate(project -> {
            if (!project.getPlugins().hasPlugin(JavaPlugin.class)) {
                project.getLogger().warn("""
                                [ErrorproneConventionPlguin] The 'java' plugin is not applied to the project.

                                ErrorproneConventionPlugin requires the Java plugin to be applied.
                                """);
                return;
            }

            var dependencies = project.getDependencies();
            dependencies.add("errorprone", "com.google.errorprone:error_prone_core:2.43.0");

            project.getTasks().withType(JavaCompile.class).configureEach(javaCompile -> {
                var compileArgs = List.of("-Xplugin:ErrorProne", "-XepDisableWarningsInGeneratedCode");
                javaCompile.getOptions().getCompilerArgs().addAll(compileArgs);
            });
        });
    }
}
