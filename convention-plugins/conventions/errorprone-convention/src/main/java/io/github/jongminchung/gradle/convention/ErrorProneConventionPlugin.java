package io.github.jongminchung.gradle.convention;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.compile.JavaCompile;
import org.jspecify.annotations.NonNull;

import net.ltgt.gradle.errorprone.CheckSeverity;
import net.ltgt.gradle.errorprone.ErrorProneOptions;
import net.ltgt.gradle.errorprone.ErrorPronePlugin;
import net.ltgt.gradle.nullaway.NullAwayExtension;
import net.ltgt.gradle.nullaway.NullAwayOptions;
import net.ltgt.gradle.nullaway.NullAwayPlugin;

import java.util.Collections;

public class ErrorProneConventionPlugin implements Plugin<@NonNull Project> {
    static final String JSPECIFY = "org.jspecify:jspecify:1.0.0";
    static final String NULLAWAY_DEPENDENCY = "com.uber.nullaway:nullaway:";
    static final String ERROR_PRONE_DEPENDENCY = "com.google.errorprone:error_prone_core:";

    @Override
    public void apply(Project target) {
        var extraErrorProne =
                target.getExtensions().create(ExtraErrorProneExtension.EXTENSION_NAME, ExtraErrorProneExtension.class);

        target.getPlugins().apply(ErrorPronePlugin.class);
        target.getPlugins().apply(NullAwayPlugin.class);

        target.getPluginManager().withPlugin("java", unused -> {
            target.getDependencies().add(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME, JSPECIFY);

            var nullawayDependency =
                    NULLAWAY_DEPENDENCY + extraErrorProne.getNullAwayVersion().get();
            target.getDependencies().add(ErrorPronePlugin.CONFIGURATION_NAME, nullawayDependency);

            var errorProneDependency = ERROR_PRONE_DEPENDENCY
                    + extraErrorProne.getErrorProneVersion().get();
            target.getDependencies().add(ErrorPronePlugin.CONFIGURATION_NAME, errorProneDependency);

            target.getTasks().withType(JavaCompile.class).configureEach(javaCompile -> {
                var compileOptions = javaCompile.getOptions();
                compileOptions.setFork(true);
                compileOptions.getForkOptions().setMemoryMaximumSize("4g");

                ExtensionAware opts = (ExtensionAware) javaCompile.getOptions();
                ErrorProneOptions errorProneOptions = opts.getExtensions().findByType(ErrorProneOptions.class);
                if (errorProneOptions == null) return;

                errorProneOptions.getExcludedPaths().set(".*/(build|out|\\.gradle)/.*");

                // errorProneOptions.getAllDisabledChecksAsWarnings().set(true); (compileJava에서 오래 점유함..)
                errorProneOptions.check("UnicodeInCode", CheckSeverity.WARN);
                configWarnOptionsToErrorOptions(errorProneOptions);

                ExtensionAware errorproneExt = (ExtensionAware) errorProneOptions;
                var nullaway = errorproneExt.getExtensions().findByType(NullAwayOptions.class);
                if (nullaway != null) {
                    if (extraErrorProne.getUseNullMarked().get()) {
                        errorProneOptions.option("NullAway:OnlyNullMarked", "true"); // Enable nullness checks only in null-marked code
                        errorProneOptions.option("NullAway:JSpecifyMode", "true"); // https://github.com/uber/NullAway/wiki/JSpecify-Support
                    }
                    nullaway.error(); // bump checks from warnings (default) to errors
                }
            });

            target.getPlugins().withType(NullAwayPlugin.class, unused2 -> {
                var nullawayExt = target.getExtensions().getByType(NullAwayExtension.class);
                nullawayExt.getOnlyNullMarked().set(extraErrorProne.getUseNullMarked());

                nullawayExt.getAnnotatedPackages().addAll(target.getProviders().provider(() -> {
                    if (extraErrorProne.getUseNullMarked().get()) {
                        return Collections.emptyList();
                    }
                    var group = String.valueOf(target.getGroup());
                    return group.isBlank() ? Collections.emptyList() : Collections.singletonList(group);
                }));
            });
        });
    }

    private static void configWarnOptionsToErrorOptions(ErrorProneOptions errorProneOptions) {
        errorProneOptions.check("UnusedMethod", CheckSeverity.ERROR);
        errorProneOptions.check("UnusedVariable", CheckSeverity.ERROR);

        errorProneOptions.check("ReferenceEquality", CheckSeverity.ERROR);
        errorProneOptions.check("BigDecimalEquals", CheckSeverity.ERROR);

        errorProneOptions.check("MissingOverride", CheckSeverity.ERROR);
    }
}
