package io.github.jongminchung.gradle.convention;

import net.ltgt.gradle.errorprone.CheckSeverity;
import net.ltgt.gradle.errorprone.ErrorProneOptions;
import net.ltgt.gradle.errorprone.ErrorPronePlugin;
import net.ltgt.gradle.nullaway.NullAwayExtension;
import net.ltgt.gradle.nullaway.NullAwayOptions;
import net.ltgt.gradle.nullaway.NullAwayPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.compile.JavaCompile;
import org.jspecify.annotations.NonNull;

public class ErrorproneConventionPlugin implements Plugin<@NonNull Project> {
    @Override
    public void apply(Project project) {
        project.getPlugins().apply(ErrorPronePlugin.class);
        project.getPlugins().apply(NullAwayPlugin.class);

        project.getPluginManager().withPlugin("java", unused -> {
            project.getDependencies().add(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME, "org.jspecify:jspecify:1.0.0");
            project.getDependencies().add(ErrorPronePlugin.CONFIGURATION_NAME, "com.uber.nullaway:nullaway:0.12.10");
            project.getDependencies().add(ErrorPronePlugin.CONFIGURATION_NAME, "com.google.errorprone:error_prone_core:2.43.0");

            var nullawayExt = project.getExtensions().findByType(NullAwayExtension.class);
            if (nullawayExt != null) {
                String group = String.valueOf(project.getGroup());
                if (!group.isBlank()) {
                    nullawayExt.getAnnotatedPackages().add(group);
                }
            }

            project.getTasks().withType(JavaCompile.class).configureEach(javaCompile -> {
                ExtensionAware opts = (ExtensionAware) javaCompile.getOptions();
                ErrorProneOptions errorprone = opts.getExtensions().findByType(ErrorProneOptions.class);
                if (errorprone == null) return;

                errorprone.getDisableWarningsInGeneratedCode().set(true);
                errorprone.getExcludedPaths().set(".*/(build|out|generated|\\.gradle)/.*");
                errorprone.check("UnicodeInCode", CheckSeverity.WARN);
                errorprone.check("UnusedVariable", CheckSeverity.WARN);

                ExtensionAware errorproneExt = (ExtensionAware) errorprone;
                var nullaway = errorproneExt.getExtensions().findByType(NullAwayOptions.class);
                if (nullaway != null) {
                    nullaway.error();
                }
            });
        });
    }
}
